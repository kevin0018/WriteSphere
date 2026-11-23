import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PublicSummary } from '../../../post/application/public-works';
import { genreLabels } from '../../../post/application/works';
import { MaritimeCover } from '../../components/layout/maritime-cover';

@Component({
  selector: 'app-work-card',
  imports: [MaritimeCover, RouterLink],
  template: `
    <article class="work-card" [attr.data-layout]="layout()">
      <app-maritime-cover [artworkKey]="work().id" [eager]="layout() === 'featured'" />
      <div class="work-card-copy">
        <p class="genre">{{ genres[work().genre] }}</p>
        @if (layout() === 'featured') {
          <h2>{{ work().title }}</h2>
        } @else {
          <h2><a [routerLink]="['/obras', work().id]">{{ work().title }}</a></h2>
        }
        <a class="byline" [routerLink]="['/autores', work().authorId]">{{ work().penName }}</a>
        @if (work().excerpt) { <p class="excerpt">{{ work().excerpt }}</p> }
        @if (layout() === 'featured') {
          <a class="work-read-link" [routerLink]="['/obras', work().id]"
            [attr.aria-label]="'Leer obra: ' + work().title">Leer obra <span aria-hidden="true">↗</span></a>
        }
      </div>
      <ng-content />
    </article>
  `,
})
export class WorkCard {
  readonly work = input.required<PublicSummary>();
  readonly layout = input<'index' | 'featured' | 'cover'>('index');
  readonly genres = genreLabels;
}
