import { Component, effect, inject, input } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { PublicResult, PublicWork } from '../../../post/application/public-works';
import { genreLabels } from '../../../post/application/works';
import { WorkText } from '../../components/layout/work-text';
import { BookmarkButton } from './bookmark-button';
import { PublicError } from './public-error';
import { ReportForm } from './report-form';
import { SessionState } from '../../../user/application/session';

@Component({
  imports: [RouterLink, WorkText, BookmarkButton, PublicError, ReportForm],
  template: `
    @if (result().value; as work) {
      <div class="column reading-page">
        <a class="editor-back" routerLink="/"><span aria-hidden="true">←</span> Portada</a>
        <article class="reading">
          <header>
            <p class="genre">{{ genres[work.genre] }}</p>
            <h1>{{ work.title }}</h1>
            <a class="byline" [routerLink]="['/autores', work.authorId]">{{ work.penName }}</a>
          </header>
          <app-work-text [body]="work.body" />
          @if (session.account().penName) {
            <div class="reading-actions">
              <app-bookmark-button [id]="work.id" />
              @for (id of [work.id]; track id) { <app-report-form [id]="id" /> }
            </div>
          }
        </article>
      </div>
    } @else { <app-public-error [status]="result().status" /> }
  `,
})
export class WorkPageComponent {
  readonly result = input.required<PublicResult<PublicWork>>();
  readonly genres = genreLabels;
  readonly session = inject(SessionState);
  private readonly title = inject(Title);

  constructor() {
    effect(() => {
      const result = this.result();
      this.title.setTitle((result.value?.title ?? 'Obra no disponible') + ' · WriteSphere');
    });
  }
}
