import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { BrnButton } from '@spartan-ng/brain/button';
import { EditorWork, Genre, genreLabels, stateLabels, WorkSummary } from '../../../post/application/works';
import { Api, apiMessage } from '../../../shared/infrastructure/api';
import { MaritimeCover } from '../../components/layout/maritime-cover';

@Component({
  imports: [BrnButton, FormsModule, MaritimeCover, RouterLink],
  template: `
    <section class="column studio">
      <header class="studio-heading">
        <h1>Mis obras</h1>
        <form class="toolbar draft-form" (ngSubmit)="create()">
          <label>Género
            <select name="genre" [(ngModel)]="genre">
              <option value="POEM">Poema</option>
              <option value="MICROFICTION">Microrrelato</option>
            </select>
          </label>
          <button brnButton type="submit" class="primary" [disabled]="creating()">
            {{ creating() ? 'Creando borrador…' : 'Nuevo borrador' }}
          </button>
        </form>
      </header>
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      @if (works(); as items) {
        @if (items.length) {
          <ul class="studio-index" aria-label="Tus obras">
            @for (work of items; track work.id) {
              <li>
                <a class="studio-entry" [routerLink]="['/escribir', work.id]">
                  <app-maritime-cover [artworkKey]="work.id" />
                  <span class="studio-work">
                    <span class="genre">{{ genres[work.genre] }}</span>
                    <span class="studio-title">{{ work.title || 'Sin título' }}</span>
                  </span>
                  <span class="work-state" [attr.data-state]="work.state">{{ states[work.state] }}</span>
                  <span class="studio-arrow">Editar <span aria-hidden="true">↗</span></span>
                </a>
              </li>
            }
          </ul>
        } @else {
          <p class="empty">Tus borradores y publicaciones aparecerán aquí.</p>
        }
      } @else if (!error()) { <p class="empty" role="status">Cargando tus obras…</p> }
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
