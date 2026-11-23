import { HttpParams } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BrnButton } from '@spartan-ng/brain/button';
import { Api, apiMessage } from '../../../shared/infrastructure/api';

type Decision = 'DISMISS' | 'HIDE';
interface Report {
  id: string;
  workId: string;
  title: string;
  author: string;
  reason: string;
  createdAt: string;
  decision: Decision | null;
  resolvedAt: string | null;
}

@Component({
  imports: [BrnButton, RouterLink],
  template: `
    <section class="column">
      <header class="atlas-heading">
        <h1>Moderación</h1>
        <div class="toolbar" role="group" aria-label="Estado de los reportes">
          <button brnButton type="button" class="secondary" [disabled]="busy()"
            [attr.aria-pressed]="!resolved()" (click)="load(false)">Pendientes</button>
          <button brnButton type="button" class="secondary" [disabled]="busy()"
            [attr.aria-pressed]="resolved()" (click)="load(true)">Resueltos</button>
        </div>
      </header>
      @if (status()) { <p class="hint" role="status">{{ status() }}</p> }
      @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      @if (reports(); as items) {
        @for (report of items; track report.id) {
          <article class="work-card">
            <h2>{{ report.title || 'Sin título' }}</h2>
            <p class="byline">{{ report.author }}</p>
            <p class="excerpt">{{ report.reason }}</p>
            @if (report.resolvedAt; as date) {
              <p>{{ report.decision === 'HIDE' ? 'Obra ocultada' : 'Reporte desestimado' }}
                · <time [attr.datetime]="date">{{ format(date) }}</time></p>
            } @else {
              <div class="toolbar">
                <a [routerLink]="['/obras', report.workId]">Abrir obra</a>
                <button brnButton type="button" class="secondary" [disabled]="busy()"
                  (click)="resolve(report.id, 'DISMISS')">Desestimar</button>
                <button brnButton type="button" class="primary" [disabled]="busy()"
                  (click)="resolve(report.id, 'HIDE')">Ocultar obra</button>
              </div>
            }
          </article>
        } @empty {
          <p class="empty">{{ resolved() ? 'Todavía no hay reportes resueltos.' : 'No hay reportes pendientes.' }}</p>
        }
      } @else if (!error()) { <p role="status">Cargando reportes…</p> }
    </section>
  `,
})
export class ModerationPage {
  private readonly api = inject(Api);
  private readonly date = new Intl.DateTimeFormat('es', { dateStyle: 'medium', timeStyle: 'short' });
  readonly reports = signal<Report[] | null>(null);
  readonly busy = signal(false);
  readonly resolved = signal(false);
  readonly status = signal('');
  readonly error = signal('');

  constructor() {
    void this.load(false);
  }

  async load(resolved: boolean): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    this.reports.set(null);
    this.resolved.set(resolved);
    this.status.set('');
    this.error.set('');
    try {
      const params = new HttpParams().set('resolved', resolved);
      this.reports.set(await this.api.get<Report[]>('/moderation/reports', params));
    } catch (error) {
      this.error.set(apiMessage(error));
    } finally {
      this.busy.set(false);
    }
  }

  async resolve(id: string, decision: Decision): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    this.status.set('');
    this.error.set('');
    try {
      await this.api.mutate('POST', '/moderation/reports/' + id + '/resolve', { decision });
      this.reports.update((reports) => reports?.filter((report) => report.id !== id) ?? null);
      this.status.set(decision === 'HIDE' ? 'Obra ocultada.' : 'Reporte desestimado.');
    } catch (error) {
      this.error.set(apiMessage(error));
    } finally {
      this.busy.set(false);
    }
  }

  format(date: string): string {
    return this.date.format(new Date(date));
  }
}
