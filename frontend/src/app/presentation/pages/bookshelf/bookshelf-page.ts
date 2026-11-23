import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BrnButton } from '@spartan-ng/brain/button';
import { PublicSummary } from '../../../post/application/public-works';
import { Api, apiMessage } from '../../../shared/infrastructure/api';
import { WorkCard } from '../reading/work-card';

@Component({
  imports: [BrnButton, WorkCard, RouterLink],
  template: `
    <section class="column bookshelf">
      <header class="atlas-heading">
        <h1>Mi estantería</h1>
        @if (works(); as items) {
          <p class="atlas-meta">{{ items.length }} {{ items.length === 1 ? 'obra guardada' : 'obras guardadas' }}</p>
        }
      </header>
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      @if (works(); as items) {
        @if (items.length) {
          <ul class="shelf-grid" aria-label="Obras guardadas">
            @for (work of items; track work.id) {
              <li>
                <app-work-card [work]="work" layout="cover">
                  <button brnButton class="text-button shelf-remove" type="button"
                    [disabled]="!!busy()" (click)="remove(work.id)"
                    [attr.aria-label]="'Quitar de mi estantería: ' + work.title">Quitar de mi estantería</button>
                </app-work-card>
              </li>
            }
          </ul>
        } @else { <p>Tu estantería está vacía. <a routerLink="/">Explorar obras</a></p> }
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
