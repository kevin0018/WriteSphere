import { HttpParams } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Api } from '../../shared/infrastructure/api';

interface Session {
  penName: string | null;
  moderator: boolean;
}

@Injectable({ providedIn: 'root' })
export class SessionState {
  private readonly api = inject(Api);
  private readonly current = signal<Session>({ penName: null, moderator: false });
  readonly account = this.current.asReadonly();

  async refresh(): Promise<Session> {
    const session = await this.api.get<Session>('/session');
    this.current.set(session);
    return session;
  }

  async login(email: string, password: string): Promise<void> {
    await this.api.mutate('POST', '/session/login', new HttpParams({ fromObject: { email, password } }));
    await this.refresh();
  }

  async logout(): Promise<void> {
    await this.api.mutate('POST', '/session/logout');
    this.current.set({ penName: null, moderator: false });
  }
}

export const requireSession: CanActivateFn = async () => {
  const session = inject(SessionState);
  const router = inject(Router);
  const account = await session.refresh().catch(() => null);
  return account?.penName ? true : router.createUrlTree(['/entrar']);
};

export const requireModerator: CanActivateFn = async () => {
  const session = inject(SessionState);
  const router = inject(Router);
  const account = await session.refresh().catch(() => null);
  if (!account?.penName) return router.createUrlTree(['/entrar']);
  return account.moderator ? true : router.createUrlTree(['/']);
};
