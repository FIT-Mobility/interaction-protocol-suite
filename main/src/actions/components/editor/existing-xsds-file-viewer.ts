import { ThunkAction } from 'redux-thunk';

import { Action } from '../..';
import { currentProjectRevisionSelector } from '../../../selectors/project';
import { State } from '../../../state';

export type Actions =
    | ReturnType<typeof closeFile>
    | ReturnType<typeof openFileFailAction>
    | ReturnType<typeof openFileFinishAction>
    | ReturnType<typeof openFileStartAction>;

export const CLOSE_FILE = 'CLOSE_FILE';
export const OPEN_FILE_FAIL = 'OPEN_FILE_FAIL';
export const OPEN_FILE_FINISH = 'OPEN_FILE_FINISH';
export const OPEN_FILE_START = 'OPEN_FILE_START';

export const closeFile = () => ({ type: CLOSE_FILE as typeof CLOSE_FILE });

export const copyToClipboard = (str: string): ThunkAction<void, State, void, Action> => () => {
    const el = document.createElement('textarea');
    el.value = str;
    el.textContent = str;
    el.setAttribute('readonly', '');
    el.style.position = 'absolute';
    el.style.left = '-9999px';
    document.body.appendChild(el);

    const selection = document.getSelection()!;
    const previouslySelected = selection.rangeCount > 0
        ? selection.getRangeAt(0)
        : null;

    const selectRange = document.createRange();
    selectRange.selectNode(el);
    selection.removeAllRanges();
    selection.addRange(selectRange);

    const copied = document.execCommand('copy');
    console.log(`Has copied: ${copied}`);

    document.body.removeChild(el);

    if (previouslySelected) {
        selection.removeAllRanges();
        selection.addRange(previouslySelected);
    }
};

export const openFile = (index: number): ThunkAction<Promise<void>, State, void, Action> =>
    async (dispatch, getState) => {
        const rev = currentProjectRevisionSelector(getState());
        if (!rev || !rev.baseUri || !rev.importableFilesList) {
            dispatch(openFileFailAction(new Error("Missing project revision.")));
            return;
        }

        const path = rev.importableFilesList[index];
        dispatch(openFileStartAction(path));

        let contents: string;

        try {
            const fileUrl = new URL(path, rev.baseUri);

            const resp = await fetch(fileUrl.toString(), { credentials: 'include' });
            if (!resp.ok) {
                if (resp.status === 404) {
                    throw new Error(`File ${path} not found.`);
                } else {
                    throw new Error(`Got invalid status code ${resp.status} from server.`);
                }
            }

            contents = await resp.text();
        } catch (err) {
            dispatch(openFileFailAction(err));
            return;
        }

        dispatch(openFileFinishAction(contents));
    };

export const openFileFailAction = (err: Error) => ({
    type: OPEN_FILE_FAIL as typeof OPEN_FILE_FAIL,
    error: true,
    payload: err,
});

export const openFileFinishAction = (contents: string) => ({
    type: OPEN_FILE_FINISH as typeof OPEN_FILE_FINISH,
    payload: contents,
});

export const openFileStartAction = (path: string) => ({
    type: OPEN_FILE_START as typeof OPEN_FILE_START,
    payload: path,
});
