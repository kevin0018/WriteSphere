import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BrnButton } from '@spartan-ng/brain/button';
import { nextPage, PublicResult, WorkPage } from '../../../post/application/public-works';
import { Genre } from '../../../post/application/works';
import { MaritimeCover } from '../../components/layout/maritime-cover';
import { PublicError } from './public-error';
import { WorkCard } from './work-card';

@Component({
  imports: [BrnButton, MaritimeCover, RouterLink, WorkCard, PublicError],
  template: `
    @if (feed().value; as page) {
      <section class="column feed">
        <header class="atlas-heading">
          <h1>Portada</h1>
          <nav class="genres" aria-label="Filtrar por género">
            <a brnButton class="filter-button" routerLink="/" [attr.aria-current]="!genre() ? 'page' : null">Todas</a>
            <a brnButton class="filter-button" routerLink="/" [queryParams]="{ genre: 'POEM' }"
              [attr.aria-current]="genre() === 'POEM' ? 'page' : null">Poemas</a>
            <a brnButton class="filter-button" routerLink="/" [queryParams]="{ genre: 'MICROFICTION' }"
              [attr.aria-current]="genre() === 'MICROFICTION' ? 'page' : null">Microrrelatos</a>
          </nav>
        </header>
        @for (work of page.items; track work.id; let first = $first) {
          <app-work-card [work]="work" [layout]="first && !before() ? 'featured' : 'index'" />
        } @empty {
          <div class="atlas-empty">
            <app-maritime-cover [eager]="true" />
            <div>
              <h2>Poemas y microrrelatos</h2>
              <p>Todavía no hay obras publicadas en esta selección.</p>
            </div>
          </div>
        }
        <nav class="pagination" aria-label="Páginas de obras">
          @if (before()) {
            <a brnButton class="secondary" routerLink="/" [queryParams]="{ genre: genre() }">Últimas publicaciones</a>
          }
          @if (page.next; as next) {
            <a brnButton class="secondary" routerLink="/" [queryParams]="{ ...pageParams(next), genre: genre() }">
              Obras anteriores →
            </a>
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
