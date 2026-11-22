import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PublicSummary } from '../../../post/application/public-works';
import { Api, apiMessage } from '../../../shared/infrastructure/api';
import { WorkCard } from '../reading/work-card';

@Component({
  imports: [WorkCard, RouterLink],
  template: `
    <section class="column">
      <h1>Mi estantería</h1>
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      @if (works(); as items) {
        @for (work of items; track work.id) {
          <app-work-card [work]="work">
            <button class="text-button" type="button" [disabled]="busy() === work.id" (click)="remove(work.id)">
              Quitar de mi estantería
            </button>
          </app-work-card>
        } @empty { <p>Tu estantería está vacía. <a routerLink="/">Explorar obras</a></p> }
      } @else if (!error()) { <p role="status">Cargando tu estantería…</p> }
    </section>
  `,
})
export class BookshelfPage {
  private readonly api = inject(Api);
  readonly works = signal<PublicSummary[] | null>(null);
  readonly busy = signal('');
  readonly error = signal('');

  constructor() {
    this.api.get<PublicSummary[]>('/bookshelf').then(this.works.set)
      .catch((error: unknown) => this.error.set(apiMessage(error)));
  }

  async remove(id: string): Promise<void> {
    if (this.busy()) return;
    this.busy.set(id);
    this.error.set('');
    try {
      await this.api.mutate('DELETE', '/bookshelf/' + id);
      this.works.update((works) => works?.filter((work) => work.id !== id) ?? null);
    } catch (error) {
      this.error.set(apiMessage(error));
    } finally {
      this.busy.set('');
    }
  }
}
