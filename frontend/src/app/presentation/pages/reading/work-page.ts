import { Component, effect, inject, input } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { PublicResult, PublicWork } from '../../../post/application/public-works';
import { genreLabels } from '../../../post/application/works';
import { WorkText } from '../../components/layout/work-text';
import { BookmarkButton } from './bookmark-button';
import { PublicError } from './public-error';

@Component({
  imports: [RouterLink, WorkText, BookmarkButton, PublicError],
  template: `
    @if (result().value; as work) {
      <article class="column reading">
        <header>
          <p class="genre">{{ genres[work.genre] }}</p>
          <h1>{{ work.title }}</h1>
          <a class="byline" [routerLink]="['/autores', work.authorId]">{{ work.penName }}</a>
        </header>
        <app-work-text [body]="work.body" />
        <div class="toolbar reading-actions">
          <app-bookmark-button [id]="work.id" />
        </div>
      </article>
    } @else { <app-public-error [status]="result().status" /> }
  `,
})
export class WorkPageComponent {
  readonly result = input.required<PublicResult<PublicWork>>();
  readonly genres = genreLabels;
  private readonly title = inject(Title);

  constructor() {
    effect(() => {
      const result = this.result();
      this.title.setTitle((result.value?.title ?? 'Obra no disponible') + ' · WriteSphere');
    });
  }
}
