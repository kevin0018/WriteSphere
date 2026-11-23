import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-public-error',
  imports: [RouterLink],
  template: `
    <section class="column">
      <h1>{{ status() === 404 ? 'Página no disponible' : 'No se pudo cargar la página' }}</h1>
      <a routerLink="/">Volver a la portada</a>
    </section>
  `,
})
export class PublicError {
  readonly status = input(404);
}
