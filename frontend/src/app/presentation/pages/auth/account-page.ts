import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BrnButton } from '@spartan-ng/brain/button';
import { BrnInput } from '@spartan-ng/brain/input';
import { Api, apiMessage } from '../../../shared/infrastructure/api';
import { passkeyMessage, Passkeys } from '../../../user/application/passkeys';
import { SessionState } from '../../../user/application/session';

@Component({
  imports: [BrnButton, BrnInput, FormsModule, RouterLink],
  template: `
    <section class="column account">
      <h1>{{ registering ? 'Crear cuenta' : 'Iniciar sesión' }}</h1>
      <form class="form-stack" (ngSubmit)="submit()">
        <label>Correo <input brnInput type="email" name="email" required maxlength="254"
          autocomplete="email" [(ngModel)]="email"></label>
        @if (registering) {
          <label>Seudónimo <input brnInput name="penName" required minlength="2" maxlength="40"
            autocomplete="nickname" [(ngModel)]="penName"></label>
        }
        <label>Contraseña
          <input brnInput type="password" name="password" required maxlength="128"
            [minlength]="registering ? 12 : 1" [autocomplete]="registering ? 'new-password' : 'current-password'"
            [attr.aria-describedby]="registering ? 'password-hint' : null" [(ngModel)]="password">
        </label>
        @if (registering) {
          <p class="hint" id="password-hint">Usa al menos 12 caracteres. Tu correo solo se usa para identificar tu cuenta.</p>
        }
        @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
        <button brnButton type="submit" class="primary" [disabled]="busy()">
          {{ registering ? 'Crear cuenta' : 'Acceder' }}
        </button>
        @if (!registering && supportsPasskeys) {
          <button brnButton type="button" class="secondary" [disabled]="busy()" (click)="signInWithPasskey()">
            Usar una passkey
          </button>
        }
      </form>
      <div class="account-switch">
        <a [routerLink]="registering ? '/entrar' : '/registro'">
          {{ registering ? 'Ya tengo cuenta' : 'Crear una cuenta' }}
        </a>
      </div>
    </section>
  `,
})
export class AccountPage {
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  private readonly session = inject(SessionState);
  private readonly passkeys = inject(Passkeys);
  readonly supportsPasskeys = this.passkeys.supported();
  readonly registering = inject(ActivatedRoute).snapshot.data['registering'] === true;
  readonly busy = signal(false);
  readonly error = signal('');
  email = '';
  penName = '';
  password = '';

  async signInWithPasskey(): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    try {
      await this.passkeys.signIn();
      await this.session.refresh();
      await this.router.navigateByUrl('/escribir');
    } catch (error) {
      this.error.set(passkeyMessage(error));
    } finally {
      this.busy.set(false);
    }
  }

  async submit(): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    try {
      if (this.registering) {
        await this.api.mutate('POST', '/accounts', {
          email: this.email.trim(), penName: this.penName.trim(), password: this.password,
        });
      }
      await this.session.login(this.email.trim(), this.password);
      await this.router.navigateByUrl('/escribir');
    } catch (error) {
      this.error.set(apiMessage(error));
    } finally {
      this.busy.set(false);
    }
  }
}
