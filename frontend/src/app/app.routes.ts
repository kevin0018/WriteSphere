import { Component } from '@angular/core';
import { Routes } from '@angular/router';
import { guardUnsavedWork } from './presentation/pages/editor/editor-page';
import { requireSession } from './user/application/session';

@Component({
  template: '<section class="column"><h1>Poemas y microrrelatos</h1></section>',
})
class HomePage {}

export const routes: Routes = [
  { path: '', pathMatch: 'full', component: HomePage },
  { path: 'entrar', title: 'Entrar · WriteSphere',
    loadComponent: () => import('./presentation/pages/auth/account-page').then((m) => m.AccountPage) },
  { path: 'registro', title: 'Crear cuenta · WriteSphere', data: { registering: true },
    loadComponent: () => import('./presentation/pages/auth/account-page').then((m) => m.AccountPage) },
  { path: 'escribir', pathMatch: 'full', title: 'Mis obras · WriteSphere', canActivate: [requireSession],
    loadComponent: () => import('./presentation/pages/editor/studio-page').then((m) => m.StudioPage) },
  { path: 'escribir/:id', title: 'Editor · WriteSphere', canActivate: [requireSession], canDeactivate: [guardUnsavedWork],
    loadComponent: () => import('./presentation/pages/editor/editor-page').then((m) => m.EditorPage) },
  { path: '**', redirectTo: '' },
];
