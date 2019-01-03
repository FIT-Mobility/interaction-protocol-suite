import { XsdOperationType } from '@ips/shared-js';

import { Action, Types } from '../../actions';
import { XsdEditorComponentState } from '../../state';

export default function(
    state: XsdEditorComponentState = {
        focusedElement: null,
        hasInitialDeltas: false,
        validationErrors: {},
        validationInProgress: false,
        validationLoadError: null,
    },
    action: Action,
): XsdEditorComponentState {
    switch (action.type) {
        case Types.AddXsdDeltas:
            if (state.hasInitialDeltas) {
                return state;
            }

            // Initially try to focus root element
            const root = action.payload.values.find(v => v.type === XsdOperationType.AddNode && !v.parentId);
            return {
                ...state,
                focusedElement: root ? root.elementId : null,
                hasInitialDeltas: true,
            };
        case Types.FocusXsdElement:
            return {
                ...state,
                focusedElement: action.payload,
            };
        case Types.LoadValidation_Start:
            return {
                ...state,
                validationErrors: {},
                validationInProgress: true,
                validationLoadError: null,
            };
        case Types.LoadValidation_Fail:
            return {
                ...state,
                validationInProgress: false,
                validationLoadError: action.payload,
            };
        case Types.LoadValidation_Finish:
            return {
                ...state,
                validationInProgress: false,
                validationErrors: action.payload,
            };
        case Types.RemoveDataType:
            if (state.focusedElement !== action.payload) {
                return state;
            }

            return {
                ...state,
                focusedElement: null,
            };
        default:
            return state;
    }
}
