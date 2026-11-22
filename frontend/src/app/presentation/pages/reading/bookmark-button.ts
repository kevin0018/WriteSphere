import { afterNextRender, Component, effect, inject, input, signal } from '@angular/core';
import { Api, apiMessage } from '../../../shared/infrastructure/api';
import { SessionState } from '../../../user/application/session';

@Component({
  selector: 'app-bookmark-button',
  template: `
    @if (session.account().penName) {
      <button class="secondary" type="button" [disabled]="busy() || saved() === null" (click)="toggle()">
        {{ saved() ? 'Quitar de mi estantería' : 'Guardar en mi estantería' }}
      </button>
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
    }
  `,
})
export class BookmarkButton {
  private readonly api = inject(Api);
  private readonly ready = signal(false);
  readonly session = inject(SessionState);
  readonly id = input.required<string>();
  readonly saved = signal<boolean | null>(null);
  readonly busy = signal(false);
  readonly error = signal('');

  constructor() {
    afterNextRender(() => this.ready.set(true));
    effect((onCleanup) => {
      if (!this.ready() || !this.session.account().penName) return;
      const id = this.id();
      let active = true;
      onCleanup(() => { active = false; });
      this.saved.set(null);
      this.error.set('');
      this.api.get<{ saved: boolean }>('/bookshelf/' + id).then(({ saved }) => {
        if (active) this.saved.set(saved);
      }).catch((error: unknown) => {
        if (active) this.error.set(apiMessage(error));
      });
    });
  }

  async toggle(): Promise<void> {
    const saved = this.saved();
    const id = this.id();
    if (saved === null || this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    try {
      await this.api.mutate(saved ? 'DELETE' : 'PUT', '/bookshelf/' + id);
      if (this.id() === id) this.saved.set(!saved);
    } catch (error) {
      if (this.id() === id) this.error.set(apiMessage(error));
    } finally {
      this.busy.set(false);
    }
  }
}
