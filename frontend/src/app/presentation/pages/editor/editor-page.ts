import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, CanDeactivateFn, RouterLink } from '@angular/router';
import { EditorWork, Genre, Manuscript, stateLabels } from '../../../post/application/works';
import { Api, apiMessage } from '../../../shared/infrastructure/api';
import { WorkText } from '../../components/layout/work-text';

type SaveState = 'saved' | 'pending' | 'saving' | 'error' | 'conflict';

@Component({
  imports: [FormsModule, RouterLink, WorkText],
  host: { '(window:beforeunload)': 'beforeUnload($event)' },
  template: `
    <section class="column editor">
      <a routerLink="/escribir">← Mis obras</a>
      @if (work(); as current) {
        <h1>{{ current.state === 'PUBLISHED' ? 'Editar obra' : 'Editar borrador' }}</h1>
        <div class="toolbar editor-bar">
          <button type="button" class="secondary" [attr.aria-pressed]="preview()" (click)="preview.set(!preview())">
            {{ preview() ? 'Volver a la edición' : 'Vista previa' }}
          </button>
          <span class="hint">{{ states[current.state] }}</span>
        </div>
        @if (preview()) {
          <article class="preview">
            <h2>{{ manuscript.title || 'Sin título' }}</h2>
            <app-work-text [body]="manuscript.body" />
          </article>
        } @else {
          <div class="form-stack">
            <label>Género <select name="genre" [disabled]="locked()" [ngModel]="manuscript.genre"
              (ngModelChange)="changeGenre($event)">
              <option value="POEM">Poema</option><option value="MICROFICTION">Microrrelato</option>
            </select></label>
            <label>Título <input name="title" maxlength="160" [disabled]="locked()" [ngModel]="manuscript.title"
              (ngModelChange)="changeText('title', $event)"></label>
            <label>Texto <textarea name="body" rows="12" maxlength="40000" [disabled]="locked()"
              [ngModel]="manuscript.body" (ngModelChange)="changeText('body', $event)"></textarea></label>
          </div>
        }
        <p aria-live="polite" class="hint">{{ saveLabels[saveState()] }}</p>
        @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
        <div class="toolbar">
          @if (saveState() === 'error') {
            <button type="button" class="secondary" (click)="save()">Reintentar guardado</button>
          }
          @if (saveState() === 'conflict') {
            <button type="button" class="secondary" [disabled]="publishing()" (click)="rebase()">
              Guardar mi texto sobre la última versión
            </button>
          }
          @if (current.state === 'DRAFT') {
            <button class="primary" [disabled]="locked() || saveState() === 'saving' || saveState() === 'conflict'"
              (click)="publish()">Publicar</button>
          }
        </div>
      } @else if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      @else { <p role="status">Cargando la obra…</p> }
    </section>
  `,
})
export class EditorPage {
  private readonly api = inject(Api);
  private readonly path = '/editor/works/' + inject(ActivatedRoute).snapshot.paramMap.get('id');
  private timer: ReturnType<typeof setTimeout> | undefined;
  private destroyed = false;
  private generation = 0;
  readonly work = signal<EditorWork | null>(null);
  readonly preview = signal(false);
  readonly saveState = signal<SaveState>('saved');
  readonly error = signal('');
  readonly publishing = signal(false);
  readonly states = stateLabels;
  readonly saveLabels: Record<SaveState, string> = {
    saved: 'Guardado', pending: 'Cambios pendientes', saving: 'Guardando…',
    error: 'Error de guardado', conflict: 'Conflicto de edición',
  };
  manuscript: Manuscript = { title: '', body: '', genre: 'POEM' };

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.destroyed = true;
      clearTimeout(this.timer);
    });
    this.api.get<EditorWork>(this.path).then((work) => {
      this.manuscript = { genre: work.genre, title: work.title, body: work.body };
      this.work.set(work);
    }).catch((error: unknown) => this.error.set(apiMessage(error)));
  }

  locked(): boolean {
    return this.publishing() || this.work()?.state === 'HIDDEN';
  }

  changeGenre(genre: Genre): void {
    this.manuscript = { ...this.manuscript, genre };
    this.changed();
  }

  changeText(field: 'title' | 'body', value: string): void {
    this.manuscript = { ...this.manuscript, [field]: value };
    this.changed();
  }

  private changed(): void {
    this.generation++;
    if (this.saveState() === 'saving' || this.saveState() === 'conflict') return;
    this.scheduleSave();
  }

  private scheduleSave(): void {
    if (this.destroyed) return;
    this.saveState.set('pending');
    clearTimeout(this.timer);
    this.timer = setTimeout(() => void this.save(), 650);
  }

  async save(): Promise<boolean> {
    const work = this.work();
    if (!work || work.state === 'HIDDEN' || this.saveState() === 'saving' || this.saveState() === 'conflict') {
      return false;
    }
    if (this.saveState() === 'saved') return true;
    clearTimeout(this.timer);
    const generation = this.generation;
    this.saveState.set('saving');
    this.error.set('');
    try {
      this.work.set(await this.api.mutate<EditorWork>('PUT', this.path, {
        ...this.manuscript, version: work.version,
      }));
      if (generation === this.generation) this.saveState.set('saved');
      else this.scheduleSave();
      return generation === this.generation;
    } catch (error) {
      this.failed(error);
      return false;
    }
  }

  private failed(error: unknown): void {
    this.saveState.set(error instanceof HttpErrorResponse && error.error?.code === 'work_conflict'
      ? 'conflict' : 'error');
    this.error.set(apiMessage(error));
  }

  async rebase(): Promise<void> {
    this.publishing.set(true);
    try {
      const work = await this.api.get<EditorWork>(this.path);
      this.work.set(work);
      if (work.state === 'HIDDEN') {
        this.error.set('Moderación ha ocultado esta obra. Puedes copiar tu texto.');
        return;
      }
      this.saveState.set('pending');
      await this.save();
    } catch (error) {
      this.error.set(apiMessage(error));
    } finally {
      this.publishing.set(false);
    }
  }

  async publish(): Promise<void> {
    this.publishing.set(true);
    try {
      if (!(await this.save())) return;
      const work = this.work();
      if (!work) return;
      this.error.set('');
      this.work.set(await this.api.mutate<EditorWork>('POST', this.path + '/publish', { version: work.version }));
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.error?.code === 'work_conflict') this.failed(error);
      else this.error.set(apiMessage(error));
    } finally {
      this.publishing.set(false);
    }
  }

  beforeUnload(event: BeforeUnloadEvent): void {
    if (this.unsaved()) event.preventDefault();
  }

  unsaved(): boolean {
    return this.saveState() !== 'saved';
  }
}

export const guardUnsavedWork: CanDeactivateFn<EditorPage> = (editor) =>
  !editor.unsaved() || window.confirm('Hay cambios sin guardar. ¿Salir del editor?');
