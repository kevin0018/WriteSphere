import { Component, computed, input } from '@angular/core';

const charts = ['chart-1.webp', 'chart-2.webp', 'chart-3.webp'];

@Component({
  selector: 'app-maritime-cover',
  template: `<img [src]="source()" alt="" width="880" height="600" [attr.loading]="eager() ? 'eager' : 'lazy'">`,
})
export class MaritimeCover {
  readonly artworkKey = input('');
  readonly eager = input(false);
  readonly source = computed(() => {
    let chart = 0;
    for (const character of this.artworkKey()) {
      chart = (chart + character.charCodeAt(0)) % charts.length;
    }
    return 'atlas/' + charts[chart];
  });
}
