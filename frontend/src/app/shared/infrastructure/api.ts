import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { inject, Injectable, InjectionToken } from '@angular/core';
import { firstValueFrom, switchMap } from 'rxjs';

export const API_ORIGIN = new InjectionToken<string>('API origin', { factory: () => '' });

@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);
  private readonly origin = inject(API_ORIGIN);

  get<T>(path: string, params?: HttpParams): Promise<T> {
    return firstValueFrom(this.http.get<T>(this.origin + '/api' + path, {
      params,
      transferCache: path.startsWith('/public/'),
    }));
  }

  mutate<T>(method: 'POST' | 'PUT' | 'DELETE', path: string, body?: unknown): Promise<T> {
    return firstValueFrom(this.http.get<{ header: string; token: string }>('/api/csrf', {
      transferCache: false,
    }).pipe(switchMap(({ header, token }) => this.http.request<T>(method, '/api' + path, {
      body,
      headers: new HttpHeaders({ [header]: token }),
    }))));
  }
}

export function apiMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) return 'Ha ocurrido un error. Inténtalo de nuevo.';
  if (error.status === 401) return 'Comprueba tus datos o inicia sesión de nuevo.';
  if (error.status === 403) return 'Tu sesión ha cambiado. Recarga la página e inténtalo de nuevo.';
  if (error.status === 404) return 'Este contenido ya no está disponible.';
  switch (error.error?.code) {
    case 'account_exists': return 'El correo o el seudónimo ya está en uso.';
    case 'work_incomplete': return 'Añade un título y un texto antes de publicar.';
    case 'work_hidden': return 'Moderación ha ocultado esta obra.';
    case 'work_conflict': return 'La obra cambió en otra pestaña. Tu texto sigue aquí.';
    case 'invalid_input': return 'Revisa los campos e inténtalo de nuevo.';
    default: return 'No se pudo completar la operación. Inténtalo de nuevo.';
  }
}
