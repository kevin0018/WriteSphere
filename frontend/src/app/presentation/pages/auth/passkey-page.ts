import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrnButton } from '@spartan-ng/brain/button';
import { BrnInput } from '@spartan-ng/brain/input';
import { Passkey, passkeyMessage, Passkeys } from '../../../user/application/passkeys';

@Component({
  imports: [BrnButton, BrnInput, FormsModule],
  template: `
    <section class="column form-page passkey-form">
      <h1>Passkeys</h1>
      <p>Usa tu dispositivo para iniciar sesión sin escribir tu contraseña.</p>
      @if (supported) {
        <form class="form-stack" (ngSubmit)="add()">
          <label>Nombre de la passkey <input brnInput name="label" required maxlength="80" [(ngModel)]="label"></label>
          <label>Contraseña actual <input brnInput name="password" type="password" required maxlength="128"
            autocomplete="current-password" [(ngModel)]="password"></label>
          <button brnButton class="primary" type="submit" [disabled]="busy() || !password || !label.trim()">Añadir passkey</button>
        </form>
      } @else {
        <p class="hint">Para añadir una passkey, abre WriteSphere en HTTPS o localhost con un navegador compatible.</p>
      }
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      @if (status()) { <p role="status">{{ status() }}</p> }
      @for (passkey of items(); track passkey.id) {
        <div class="index-row">
          <span>{{ passkey.label }}</span>
          <button brnButton type="button" class="text-button" [disabled]="busy()" (click)="remove(passkey.id)">
            Eliminar passkey<span class="sr-only"> {{ passkey.label }}</span>
          </button>
        </div>
      }
    </section>
  `,
})
export class PasskeyPage {
  private readonly passkeys = inject(Passkeys);
  readonly supported = this.passkeys.supported();
  readonly items = signal<Passkey[]>([]);
  readonly busy = signal(false);
  readonly error = signal('');
  readonly status = signal('');
  label = '';
  password = '';

  constructor() {
    void this.load();
  }

  private async load(): Promise<void> {
    try {
      this.items.set(await this.passkeys.list());
    } catch (error) {
      this.error.set(passkeyMessage(error));
    }
  }

  async add(): Promise<void> {
    if (this.busy() || !this.password || !this.label.trim()) return;
    this.busy.set(true);
    this.error.set('');
    this.status.set('');
    const password = this.password;
    this.password = '';
    try {
      await this.passkeys.add(password, this.label.trim());
      this.label = '';
      this.status.set('Passkey añadida.');
      await this.load();
    } catch (error) {
      this.error.set(passkeyMessage(error));
    } finally {
      this.busy.set(false);
    }
  }

  async remove(id: string): Promise<void> {
    if (this.busy() || !window.confirm('¿Eliminar esta passkey de tu cuenta?')) return;
    this.busy.set(true);
    this.error.set('');
    this.status.set('');
    try {
      await this.passkeys.remove(id);
      this.items.update(items => items.filter(item => item.id !== id));
      this.status.set('Passkey eliminada.');
    } catch (error) {
      this.error.set(passkeyMessage(error));
    } finally {
      this.busy.set(false);
    }
  }
}
