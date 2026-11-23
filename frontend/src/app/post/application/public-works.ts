import { HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, RESPONSE_INIT } from '@angular/core';
import { ActivatedRouteSnapshot, ResolveFn } from '@angular/router';
import { Api } from '../../shared/infrastructure/api';
import { Genre } from './works';

interface Publication {
  id: string;
  authorId: string;
  penName: string;
  genre: Genre;
  title: string;
  publishedAt: string;
}

export interface PublicWork extends Publication { body: string; }
export interface PublicSummary extends Publication { excerpt: string; }

interface Cursor { publishedAt: string; id: string; }
export interface WorkPage { items: PublicSummary[]; next: Cursor | null; }
export interface Profile { id: string; penName: string; works: WorkPage; }
export type PublicResult<T> = { value: T; status: 200 } | { value: null; status: number };

export function nextPage(cursor: Cursor): Record<string, string> {
  return { before: cursor.publishedAt, beforeId: cursor.id };
}

function pageParams(route: ActivatedRouteSnapshot): HttpParams {
  let params = new HttpParams();
  for (const key of ['genre', 'before', 'beforeId']) {
    const value = route.queryParamMap.get(key);
    if (value) params = params.set(key, value);
  }
  return params;
}

function publicData<T>(
  path: (route: ActivatedRouteSnapshot) => string,
  paginate = false,
): ResolveFn<PublicResult<T>> {
  return async (route) => {
    const api = inject(Api);
    const response = inject(RESPONSE_INIT, { optional: true });
    try {
      const value = await api.get<T>(path(route), paginate ? pageParams(route) : undefined);
      return { value, status: 200 };
    } catch (error) {
      const status = error instanceof HttpErrorResponse && [400, 404].includes(error.status)
        ? error.status : 503;
      if (response) response.status = status;
      return { value: null, status };
    }
  };
}

function idOf(route: ActivatedRouteSnapshot): string {
  return encodeURIComponent(route.paramMap.get('id') ?? '');
}

export const resolveFeed = publicData<WorkPage>(() => '/public/works', true);
export const resolveProfile = publicData<Profile>((route) => '/public/authors/' + idOf(route), true);
export const resolveWork = publicData<PublicWork>((route) => '/public/works/' + idOf(route));
