import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PublicSummary } from '../../../post/application/public-works';
import { genreLabels } from '../../../post/application/works';

@Component({
  selector: 'app-work-card',
  imports: [RouterLink],
  template: `
    <article class="work-card">
      <p class="genre">{{ genres[work().genre] }}</p>
      <h2><a [routerLink]="['/obras', work().id]">{{ work().title }}</a></h2>
      <a class="byline" [routerLink]="['/autores', work().authorId]">{{ work().penName }}</a>
      <p class="excerpt">{{ work().excerpt }}</p>
      <ng-content />
    </article>
  `,
})
export class WorkCard {
  readonly work = input.required<PublicSummary>();
  readonly genres = genreLabels;
}
