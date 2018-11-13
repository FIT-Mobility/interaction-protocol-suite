import { Store } from 'redux';
import { LocationChangedAction, LOCATION_CHANGED } from 'redux-little-router';

import { Action } from '../actions';
import { closeFile, openFile } from '../actions/components/editor/existing-xsds-file-viewer';
import { State } from '../state';

let currentFileIdx: number | undefined;

function handleLocationChanged(store: Store<State>, ac: LocationChangedAction) {
    const urlParam = ac.payload.params &&
        ac.payload.params.fileIndex;
    const urlFileIdx = urlParam
        ? Number(urlParam)
        : undefined;

    if (urlFileIdx !== undefined) { // can be 0, but 0 is falsey
        if (currentFileIdx !== urlFileIdx) {
            currentFileIdx = urlFileIdx;
            store.dispatch(openFile(urlFileIdx) as any);
        }
    } else if (currentFileIdx !== undefined) {
        currentFileIdx = undefined;
        store.dispatch(closeFile());
    }
}

export default (store: Store<State>) => (next: (ac: any) => any) => (action: Action) => {
    next(action);

    if (action.type === LOCATION_CHANGED) {
        handleLocationChanged(store, action);
    }
};
