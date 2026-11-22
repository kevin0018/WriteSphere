import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { nextPage, Profile, PublicResult } from '../../../post/application/public-works';
import { PublicError } from './public-error';
import { WorkCard } from './work-card';

@Component({
  imports: [RouterLink, WorkCard, PublicError],
  template: `
    @if (profile().value; as author) {
      <section class="column">
        <h1>{{ author.penName }}</h1>
        @for (work of author.works.items; track work.id) {
          <app-work-card [work]="work" />
        } @empty { <p class="empty">Este perfil todavía no tiene obras publicadas.</p> }
        <nav class="pagination" aria-label="Páginas de obras">
          @if (before()) { <a [routerLink]="['/autores', author.id]">Últimas publicaciones</a> }
          @if (author.works.next; as next) {
            <a [routerLink]="['/autores', author.id]" [queryParams]="pageParams(next)">Obras anteriores →</a>
          }
        </nav>
      </section>
    } @else { <app-public-error [status]="profile().status" /> }
  `,
})
export class ProfilePage {
  readonly profile = input.required<PublicResult<Profile>>();
  readonly before = input<string>();
  readonly pageParams = nextPage;
}
