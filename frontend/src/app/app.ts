import { afterNextRender, Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { apiMessage } from './shared/infrastructure/api';
import { SessionState } from './user/application/session';

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterOutlet],
  template: `
    <header class="masthead">
      <div class="masthead-content">
        <a class="wordmark" routerLink="/">WriteSphere</a>
        <nav aria-label="Navegación principal">
          @if (session.account().penName) {
            <a routerLink="/estanteria">Estantería</a>
            <a routerLink="/escribir">Mis obras</a>
            <a routerLink="/cuenta/passkeys">Passkeys</a>
            @if (session.account().moderator) { <a routerLink="/moderacion">Moderación</a> }
            <button type="button" class="text-button" [disabled]="busy()" (click)="logout()">Salir</button>
          } @else {
            <a routerLink="/entrar">Entrar</a>
            <a routerLink="/registro">Crear cuenta</a>
          }
        </nav>
      </div>
    </header>
    @if (error()) { <p class="column error" role="alert">{{ error() }}</p> }
    <main id="contenido"><router-outlet /></main>
  `,
})
export class App {
  readonly session = inject(SessionState);
  private readonly router = inject(Router);
  readonly error = signal('');
  readonly busy = signal(false);

  constructor() {
    afterNextRender(() => void this.session.refresh().catch(() => undefined));
  }

  async logout(): Promise<void> {
    if (!(await this.router.navigateByUrl('/'))) return;
    this.error.set('');
    this.busy.set(true);
    try {
      await this.session.logout();
    } catch (error) {
      this.error.set(apiMessage(error));
    } finally {
      this.busy.set(false);
    }
  }
}
