import {
    AddAttributeXsdOperation,
    AddNodeXsdOperation,
    XsdOperationType,
} from 'omp-schema';
import { ElementTypes } from 'omp-schema/schema-tree';
import { ThunkAction } from 'redux-thunk';
import { v4 as uuid } from 'uuid';

import { Action, PayloadAction, Types } from '../..';
import { State } from '../../../state';
import { updateXsd } from '../../sync';

export type XsdEditorActions =
    | FocusXsdElementAction;

export interface FocusXsdElementAction extends PayloadAction<string> {
    type: Types.FocusXsdElement;
}

export function addAttribute(
    elementId: string,
    name: string,
    position: number,
): ThunkAction<void, State, void, Action> {
    return updateXsd({
        type: XsdOperationType.AddAttribute,
        attributeId: uuid(),
        createdOn: Date.now(),
        elementId,
        name,
        position,
        value: '',
    } as AddAttributeXsdOperation);
}

export function addNode(
    parentId: string,
    schemaId: ElementTypes,
    position: number,
): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        const { name } = getState().projectData.xsdSchema.elements[schemaId];

        const op: AddNodeXsdOperation = {
            type: XsdOperationType.AddNode,
            createdOn: Date.now(),
            elementId: uuid(),
            elementType: schemaId,
            name,
            parentId,
            position,
        };

        dispatch(updateXsd(op));
        dispatch(focusNode(op.elementId));
    };
}

export function editAttributeName(
    elementId: string,
    attributeId: string,
    name: string,
): ThunkAction<void, State, void, Action> {
    return updateXsd({
        type: XsdOperationType.EditAttribute,
        attributeId,
        elementId,
        name,
    });
}

export function editAttributeValue(
    elementId: string,
    attributeId: string,
    value: string,
): ThunkAction<void, State, void, Action> {
    return updateXsd({
        type: XsdOperationType.EditAttribute,
        attributeId,
        elementId,
        value,
    });
}

export function editContent(elementId: string, newContent: string): ThunkAction<void, State, void, Action> {
    return updateXsd({
        type: XsdOperationType.EditTextContent,
        elementId,
        newContent,
    });
}

export function focusNode(elId: string): FocusXsdElementAction {
    return {
        type: Types.FocusXsdElement,
        payload: elId,
    };
}

export function moveNode(elementId: string, newPosition: number): ThunkAction<void, State, void, Action> {
    return updateXsd({
        type: XsdOperationType.MoveNode,
        elementId,
        newPosition,
    });
}

export function removeAttribute(elementId: string, attributeId: string): ThunkAction<void, State, void, Action> {
    return updateXsd({
        type: XsdOperationType.RemoveAttribute,
        elementId,
        attributeId,
    });
}

export function removeNode(elementId: string): ThunkAction<void, State, void, Action> {
    return updateXsd({
        type: XsdOperationType.RemoveNode,
        elementId,
    });
}

export { updateXsd };
