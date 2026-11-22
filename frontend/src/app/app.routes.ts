import { Routes } from '@angular/router';
import { resolveFeed, resolveProfile, resolveWork } from './post/application/public-works';
import { PublicError } from './presentation/pages/reading/public-error';
import { guardUnsavedWork } from './presentation/pages/editor/editor-page';
import { requireSession } from './user/application/session';

export const routes: Routes = [
  { path: '', pathMatch: 'full', title: 'WriteSphere', resolve: { feed: resolveFeed },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
    loadComponent: () => import('./presentation/pages/reading/feed-page').then((m) => m.FeedPage) },
  { path: 'obras/:id', resolve: { result: resolveWork },
    loadComponent: () => import('./presentation/pages/reading/work-page').then((m) => m.WorkPageComponent) },
  { path: 'autores/:id', title: 'Perfil de autor · WriteSphere', resolve: { profile: resolveProfile },
    runGuardsAndResolvers: 'paramsOrQueryParamsChange',
    loadComponent: () => import('./presentation/pages/reading/profile-page').then((m) => m.ProfilePage) },
  { path: 'estanteria', title: 'Mi estantería · WriteSphere', canActivate: [requireSession],
    loadComponent: () => import('./presentation/pages/bookshelf/bookshelf-page').then((m) => m.BookshelfPage) },
  { path: 'entrar', title: 'Entrar · WriteSphere',
    loadComponent: () => import('./presentation/pages/auth/account-page').then((m) => m.AccountPage) },
  { path: 'registro', title: 'Crear cuenta · WriteSphere', data: { registering: true },
    loadComponent: () => import('./presentation/pages/auth/account-page').then((m) => m.AccountPage) },
  { path: 'escribir', pathMatch: 'full', title: 'Mis obras · WriteSphere', canActivate: [requireSession],
    loadComponent: () => import('./presentation/pages/editor/studio-page').then((m) => m.StudioPage) },
  { path: 'escribir/:id', title: 'Editor · WriteSphere', canActivate: [requireSession], canDeactivate: [guardUnsavedWork],
    loadComponent: () => import('./presentation/pages/editor/editor-page').then((m) => m.EditorPage) },
  { path: '**', component: PublicError },
];
