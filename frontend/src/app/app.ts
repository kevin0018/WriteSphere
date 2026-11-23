import { afterNextRender, Component, computed, DestroyRef, DOCUMENT, inject, signal } from '@angular/core';
import { isActive, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { BrnButton } from '@spartan-ng/brain/button';
import { BrnSwitch, BrnSwitchThumb } from '@spartan-ng/brain/switch';
import { apiMessage } from './shared/infrastructure/api';
import { SessionState } from './user/application/session';

@Component({
  selector: 'app-root',
  imports: [BrnButton, BrnSwitch, BrnSwitchThumb, RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <header class="masthead">
      <div class="masthead-content">
        <a class="wordmark" routerLink="/">Write<span>Sphere</span></a>
        @if (session.account().penName || !onAccountScreen()) {
          <nav aria-label="Navegación principal">
            @if (session.account().penName) {
              <a brnButton class="nav-button" routerLink="/"
                [class.is-current]="onHome()" [attr.aria-current]="onHome() ? 'page' : null">Portada</a>
              <a brnButton class="nav-button" routerLink="/estanteria" routerLinkActive="is-current"
                ariaCurrentWhenActive="page">Estantería</a>
              <a brnButton class="nav-button" routerLink="/escribir" routerLinkActive="is-current"
                ariaCurrentWhenActive="page">Mis obras</a>
              <a brnButton class="nav-button" routerLink="/cuenta/passkeys" routerLinkActive="is-current"
                ariaCurrentWhenActive="page">Passkeys</a>
              @if (session.account().moderator) {
                <a brnButton class="nav-button" routerLink="/moderacion" routerLinkActive="is-current"
                  ariaCurrentWhenActive="page">Moderación</a>
              }
              <button brnButton type="button" class="nav-button" [disabled]="busy()" (click)="logout()">Salir</button>
            } @else {
              <a brnButton class="nav-button" routerLink="/entrar">Iniciar sesión</a>
              <a brnButton class="nav-button primary" routerLink="/registro">Crear cuenta</a>
            }
          </nav>
        }
        <brn-switch class="theme-switch" aria-label="Modo oscuro"
          [checked]="darkMode()" (checkedChange)="setTheme($event)">
          <span brnSwitchThumb class="theme-thumb" aria-hidden="true"></span>
          <span class="theme-icon theme-sun" aria-hidden="true"></span>
          <span class="theme-icon theme-moon" aria-hidden="true"></span>
        </brn-switch>
      </div>
    </header>
    @if (error()) { <p class="column error" role="alert">{{ error() }}</p> }
    <main id="contenido"><router-outlet /></main>
  `,
})
export class App {
  readonly session = inject(SessionState);
  private readonly router = inject(Router);
  readonly onHome = isActive('/', this.router, { paths: 'exact', queryParams: 'ignored' });
  private readonly onLogin = isActive('/entrar', this.router, { paths: 'exact' });
  private readonly onRegistration = isActive('/registro', this.router, { paths: 'exact' });
  readonly onAccountScreen = computed(() => this.onLogin() || this.onRegistration());
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  readonly darkMode = signal(false);
  readonly error = signal('');
  readonly busy = signal(false);

  constructor() {
    afterNextRender(() => {
      void this.session.refresh().catch(() => undefined);

      const preference = this.document.defaultView?.matchMedia?.('(prefers-color-scheme: dark)');
      const syncTheme = () => {
        const theme = this.document.documentElement.dataset['theme'];
        this.darkMode.set(theme ? theme === 'dark' : preference?.matches ?? false);
      };
      syncTheme();
      preference?.addEventListener('change', syncTheme);
      this.destroyRef.onDestroy(() => preference?.removeEventListener('change', syncTheme));
    });
  }

  setTheme(dark: boolean): void {
    const theme = dark ? 'dark' : 'light';
    this.document.documentElement.dataset['theme'] = theme;
    this.darkMode.set(dark);
    try {
      this.document.defaultView?.localStorage.setItem('writesphere-theme', theme);
    } catch {
      // The selected theme still applies when storage is unavailable.
    }
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
