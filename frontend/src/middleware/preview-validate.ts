/*
 * This redux middleware generates the preview when the user changes something.
 */

import debounce from 'lodash-es/debounce';
import isEmpty from 'lodash-es/isEmpty';
import { Store } from 'redux';
import { LOCATION_CHANGED } from 'redux-little-router';

import { Action, Types } from '../actions';
import { generatePreview } from '../actions/components/editor/preview';
import { validateXsd } from '../actions/components/editor/validation';
import { ChangeConnectionStateAction } from '../actions/sync';
import { ConnectionState, State } from '../state';

function generate(store: Store<State, any>) {
    store.dispatch(generatePreview());
    store.dispatch(validateXsd());
}

const debouncedGenerate = debounce(generate, 1000);

export default (store: Store<State>) => (next: (ac: any) => any) => (action: Action) => {
    next(action);

    const { components, router, projectData } = store.getState();
    if (!router.params ||
        !router.params.projectUrlSlug ||
        !router.params.revisionUrlSlug ||
        !components.preview.autoGenerate ||
        isEmpty(projectData.xsd)) {
        return;
    }

    switch (action.type) {
        case Types.AddDataType:
        case Types.AddFunction:
        case Types.AddSequence:
        case Types.AddService:
        case Types.AddXsdDeltas:
        case Types.OpenProject_Finish:
        case Types.RemoveDataType:
        case Types.RemoveFunction:
        case Types.RemoveSequence:
        case Types.RemoveService:
        case Types.SetAutoGeneratePreview: // Also run if user has just turned on preview generation
        case Types.UpdateDataType:
        case Types.UpdateFunction:
        case Types.UpdateProject:
        case Types.UpdateProjectRevision:
        case Types.UpdateSequence:
        case Types.UpdateService:
            debouncedGenerate(store);
            break;
        case Types.ChangeConnectionState:
            if ((action as ChangeConnectionStateAction).payload === ConnectionState.Connected) {
                debouncedGenerate(store);
            }
            break;
        case LOCATION_CHANGED:
            const s = store.getState();

            // Location changes only affect the preview if the language changes
            if (!s.router.previous || s.router.query.lang !== s.router.previous.query.lang) {
                debouncedGenerate(store);
            }
            break;
    }
};
