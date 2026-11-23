import { Component, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api, apiMessage } from '../../../shared/infrastructure/api';

@Component({
  selector: 'app-report-form',
  imports: [FormsModule],
  template: `
    <details #panel>
      <summary>Reportar obra</summary>
      <form class="form-stack" (ngSubmit)="report(panel)">
        <label>Motivo <textarea name="reason" rows="4" required maxlength="1000" [(ngModel)]="reason"></textarea></label>
        <button class="secondary" [disabled]="busy() || !reason.trim()">Enviar reporte</button>
        @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
      </form>
    </details>
    @if (sent()) { <p role="status">Reporte recibido.</p> }
  `,
})
export class ReportForm {
  private readonly api = inject(Api);
  readonly id = input.required<string>();
  readonly error = signal('');
  readonly sent = signal(false);
  readonly busy = signal(false);
  reason = '';

  async report(panel: HTMLDetailsElement): Promise<void> {
    if (this.busy() || !this.reason.trim()) return;
    this.busy.set(true);
    this.error.set('');
    this.sent.set(false);
    try {
      await this.api.mutate('POST', '/works/' + this.id() + '/reports', { reason: this.reason.trim() });
      this.sent.set(true);
      this.reason = '';
      panel.open = false;
      panel.querySelector('summary')?.focus();
    } catch (error) {
      this.error.set(apiMessage(error));
    } finally {
      this.busy.set(false);
    }
  }
}
