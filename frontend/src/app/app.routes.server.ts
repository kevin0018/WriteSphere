import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: 'entrar', renderMode: RenderMode.Client },
  { path: 'registro', renderMode: RenderMode.Client },
  { path: 'escribir', renderMode: RenderMode.Client },
  { path: 'escribir/:id', renderMode: RenderMode.Client },
  { path: '**', renderMode: RenderMode.Server },
];
