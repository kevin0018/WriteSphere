export type Genre = 'POEM' | 'MICROFICTION';
export type WorkState = 'DRAFT' | 'PUBLISHED' | 'HIDDEN';

export interface Manuscript {
  genre: Genre;
  title: string;
  body: string;
}

export interface EditorWork extends Manuscript {
  id: string;
  version: number;
  state: WorkState;
}

export type WorkSummary = Pick<EditorWork, 'id' | 'title' | 'genre' | 'state'>;

export const genreLabels: Record<Genre, string> = { POEM: 'Poema', MICROFICTION: 'Microrrelato' };
export const stateLabels: Record<WorkState, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicada',
  HIDDEN: 'Ocultada',
};
