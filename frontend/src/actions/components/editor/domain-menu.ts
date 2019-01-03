import { DomainItemType } from '@ips/shared-js';
import { ThunkAction } from 'redux-thunk';

import { pushInProject, Action, PayloadAction, Types } from '../..';
import { State } from '../../../state';
import {
    createFunction as createFn,
    createSequence as createSq,
    createService as createSv,
    deleteFunction,
    deleteSequence,
    deleteService,
    toggleServiceFunction,
    toggleServiceSequence,
} from '../../sync';
import { focusComponent } from "../with-focus";

export type DomainMenuActions =
    | ToggleDomainItemMenuAction;

export interface ToggleDomainItemMenuAction extends PayloadAction<DomainItemType> {
    type: Types.ToggleDomainItemMenu;
}

export function createItem(
    item: DomainItemType.Function | DomainItemType.Sequence | DomainItemType.Service,
): ThunkAction<void, State, void, Action> {
    return dispatch => {
        let id;
        let focusId: string | null = null;
        switch (item) {
            case DomainItemType.Function:
                id = dispatch(createFn('New Function', '', ''));
                focusId = 'functionname';
                break;
            case DomainItemType.Sequence:
                id = dispatch(createSq('New Sequence', []));
                focusId = 'sequencename';
                break;
            case DomainItemType.Service:
                id = dispatch(createSv('New Service', [], []));
                focusId = 'servicename';
                break;
        }

        // Ensure the data has processed in yjs
        setTimeout(() => dispatch(() => {
            dispatch(openItem(item, id));
            dispatch(focusComponent(focusId));
        }), 20);
    };
}

export function deleteItem(
    type: DomainItemType.Function | DomainItemType.Sequence | DomainItemType.Service,
    id: string,
): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        // Clean up dangling references and remove item

        const state = getState();
        const { services } = state.projectData;
        const { functionId, sequenceId, serviceId } = state.router.params || {} as any;

        // Redirect away from item
        if (id === functionId || id === sequenceId || id === serviceId) {
            dispatch(pushInProject(''));
        }

        switch (type) {
            case DomainItemType.Function:
                Object.keys(services)
                    .map(k => services[k])
                    .filter(svc => svc.functions.indexOf(id) !== -1)
                    .forEach(svc => dispatch(toggleServiceFunction(svc.id, id)));

                dispatch(deleteFunction(id));
                break;

            case DomainItemType.Sequence:
                Object.keys(services)
                    .map(k => services[k])
                    .filter(svc => svc.sequences.indexOf(id) !== -1)
                    .forEach(svc => dispatch(toggleServiceSequence(svc.id, id)));

                dispatch(deleteSequence(id));
                break;

            case DomainItemType.Service:
                dispatch(deleteService(id));
                break;
        }
    };
}

export function openItem(type: DomainItemType, id: string): Action {
    function getSlug(type: DomainItemType): string {
        switch (type) {
            case DomainItemType.DataType:
                return 'data-types';
            case DomainItemType.Function:
                return 'functions';
            case DomainItemType.Sequence:
                return 'sequences';
            case DomainItemType.Service:
                return 'services';
        }
    }

    return pushInProject(`${getSlug(type)}/${id}`);
}

export function openProjectDocs(): Action {
    return pushInProject('project');
}

export function openXsdEditor(): Action {
    return pushInProject('xsd');
}

export function toggleMenu(forItem: DomainItemType): ToggleDomainItemMenuAction {
    return {
        type: Types.ToggleDomainItemMenu,
        payload: forItem,
    };
}
