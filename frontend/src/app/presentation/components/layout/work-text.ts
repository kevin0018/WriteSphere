import { Component, input } from '@angular/core';

@Component({
  selector: 'app-work-text',
  template: '<div class="work-text">{{ body() }}</div>',
})
export class WorkText {
  readonly body = input.required<string>();
}
