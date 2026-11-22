import { CanDeactivateFn } from '@angular/router';
import type { EditorPage } from '../pages/editor/editor-page';

export const guardUnsavedWork: CanDeactivateFn<EditorPage> = (editor) =>
  !editor.unsaved() || window.confirm('Hay cambios sin guardar. ¿Salir del editor?');
