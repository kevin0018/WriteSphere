import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { nextPage, PublicResult, WorkPage } from '../../../post/application/public-works';
import { Genre } from '../../../post/application/works';
import { PublicError } from './public-error';
import { WorkCard } from './work-card';

@Component({
  imports: [RouterLink, WorkCard, PublicError],
  template: `
    @if (feed().value; as page) {
      <section class="column">
        <h1>Poemas y<br class="mast-break"> microrrelatos</h1>
        <nav class="genres" aria-label="Filtrar por género">
          <a routerLink="/" [attr.aria-current]="!genre() ? 'page' : null">Todas</a>
          <a routerLink="/" [queryParams]="{ genre: 'POEM' }"
            [attr.aria-current]="genre() === 'POEM' ? 'page' : null">Poemas</a>
          <a routerLink="/" [queryParams]="{ genre: 'MICROFICTION' }"
            [attr.aria-current]="genre() === 'MICROFICTION' ? 'page' : null">Microrrelatos</a>
        </nav>
        @for (work of page.items; track work.id) {
          <app-work-card [work]="work" />
        } @empty { <p class="empty">Todavía no hay obras publicadas en esta selección.</p> }
        <nav class="pagination" aria-label="Páginas de obras">
          @if (before()) {
            <a routerLink="/" [queryParams]="{ genre: genre() }">Últimas publicaciones</a>
          }
          @if (page.next; as next) {
            <a routerLink="/" [queryParams]="{ ...pageParams(next), genre: genre() }">Obras anteriores →</a>
          }
        </nav>
      </section>
    } @else { <app-public-error [status]="feed().status" /> }
  `,
})
export class FeedPage {
  readonly feed = input.required<PublicResult<WorkPage>>();
  readonly genre = input<Genre>();
  readonly before = input<string>();
  readonly pageParams = nextPage;
}
