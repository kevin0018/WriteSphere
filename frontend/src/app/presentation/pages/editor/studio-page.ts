import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EditorWork, Genre, genreLabels, stateLabels, WorkSummary } from '../../../post/application/works';
import { Api, apiMessage } from '../../../shared/infrastructure/api';

@Component({
  imports: [FormsModule, RouterLink],
  template: `
    <section class="column">
      <h1>Mis obras</h1>
      <form class="toolbar" (ngSubmit)="create()">
        <label>Género <select name="genre" [(ngModel)]="genre">
          <option value="POEM">Poema</option><option value="MICROFICTION">Microrrelato</option>
        </select></label>
        <button class="primary" [disabled]="creating()">Nuevo borrador</button>
      </form>
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      @if (works(); as items) {
        @for (work of items; track work.id) {
          <div class="index-row">
            <a [routerLink]="['/escribir', work.id]">{{ work.title || 'Sin título' }}</a>
            <span>{{ genres[work.genre] }} · {{ states[work.state] }}</span>
          </div>
        } @empty {
          <p>Tus borradores y publicaciones aparecerán aquí.</p>
        }
      } @else if (!error()) { <p role="status">Cargando tus obras…</p> }
    </section>
  `,
})
export class StudioPage {
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  readonly works = signal<WorkSummary[] | null>(null);
  readonly error = signal('');
  readonly creating = signal(false);
  readonly genres = genreLabels;
  readonly states = stateLabels;
  genre: Genre = 'POEM';

  constructor() {
    this.api.get<WorkSummary[]>('/editor/works').then(this.works.set)
      .catch((error: unknown) => this.error.set(apiMessage(error)));
  }

  async create(): Promise<void> {
    this.creating.set(true);
    this.error.set('');
    try {
      const work = await this.api.mutate<EditorWork>('POST', '/editor/works', { genre: this.genre });
      await this.router.navigate(['/escribir', work.id]);
    } catch (error) {
      this.error.set(apiMessage(error));
    } finally {
      this.creating.set(false);
    }
  }
}
