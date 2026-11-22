import { Component, input, output } from '@angular/core';
import {
  BrnDialog,
  BrnDialogClose,
  BrnDialogContent,
  BrnDialogDescription,
  BrnDialogOverlay,
  BrnDialogTitle,
  BrnDialogTrigger,
} from '@spartan-ng/brain/dialog';

@Component({
  selector: 'app-withdrawal-dialog',
  imports: [BrnDialog, BrnDialogClose, BrnDialogContent, BrnDialogDescription, BrnDialogOverlay,
    BrnDialogTitle, BrnDialogTrigger],
  template: `
    <brn-dialog #dialog="brnDialog" role="alertdialog" [aria-modal]="true" (closed)="closed($event)">
      <button brnDialogTrigger class="secondary" [disabled]="disabled()">Retirar obra</button>
      <brn-dialog-overlay class="withdrawal-backdrop" />
      <ng-template brnDialogContent>
        <section class="withdrawal-dialog">
          <h2 brnDialogTitle>¿Retirar esta obra?</h2>
          <p brnDialogDescription>Volverá a tus borradores y dejará de estar disponible en su enlace
            y en las estanterías.</p>
          <div class="toolbar">
            <button type="button" class="secondary" brnDialogClose>Cancelar</button>
            <button type="button" class="primary" (click)="dialog.close(true)">Retirar obra</button>
          </div>
        </section>
      </ng-template>
    </brn-dialog>
  `,
})
export class WithdrawalDialog {
  readonly disabled = input(false);
  readonly confirmed = output<void>();

  closed(result: unknown): void {
    if (result === true) this.confirmed.emit();
  }
}
