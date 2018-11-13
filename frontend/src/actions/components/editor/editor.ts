import Quill from 'quill';
import { ThunkAction } from 'redux-thunk';
import 'url-search-params';

import { push, Action, Types } from '../..';
import { EditorMode, State } from '../../../state';
import { store } from '../../../store';

import { generatePreview } from './preview';

const Delta = Quill.import('delta');

window.addEventListener(Types.ClickCommentReference, (ev: CustomEvent) => {
    store.dispatch(push({ query: { editor: EditorMode.Documentation }}));

    const el = document.getElementById(ev.detail);
    el && el.scrollIntoView();
});

export function changeEditorMode(mode: EditorMode): Action {
    return push({ query: { editor: mode } });
}

export function createCommentReference(refId: string, text: string): ThunkAction<void, State, void, Action> {
    return dispatch => {
        dispatch(push({ query: { editor: EditorMode.Comments } }));

        const el = document.querySelector('#comment-editor .ql-container')!;
        if (!el) {
            throw new Error("Missing comment editor");
        }
        const q: Quill = Quill.find(el);

        const delta = new Delta()
            .insert(text, { ref: refId })
            .insert("\n", { blockquote: true })
            .insert("\n", { blockquote: false });
        q.setContents(delta);
        el.scrollIntoView();
        q.setSelection(q.getLength() - 1, 0); // q.focus() would jump to the beginning
    };
}

export function onQuillTextChange(): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        if (!getState().components.preview.autoGenerate) {
            return;
        }

        return dispatch(generatePreview());
    };
}
