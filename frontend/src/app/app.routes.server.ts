import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: 'entrar', renderMode: RenderMode.Client },
  { path: 'cuenta/passkeys', renderMode: RenderMode.Client },
  { path: 'registro', renderMode: RenderMode.Client },
  { path: 'escribir', renderMode: RenderMode.Client },
  { path: 'escribir/:id', renderMode: RenderMode.Client },
  { path: 'moderacion', renderMode: RenderMode.Client },
  { path: 'estanteria', renderMode: RenderMode.Client },
  { path: '', renderMode: RenderMode.Server },
  { path: 'obras/:id', renderMode: RenderMode.Server },
  { path: 'autores/:id', renderMode: RenderMode.Server },
  { path: '**', renderMode: RenderMode.Server, status: 404 },
];
