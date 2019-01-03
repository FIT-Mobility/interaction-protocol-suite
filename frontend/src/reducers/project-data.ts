import { IdAble } from '@ips/shared-js';
import schema from '@ips/shared-js/schema-tree';
import { applyDelta } from '@ips/shared-js/xsd';
import { Dictionary } from 'lodash';
import mapValues from 'lodash-es/mapValues';
import omit from 'lodash-es/omit';

import { Action, Types, Update } from '../actions';
import { ProjectData } from '../state';

function createDefault(): ProjectData {
    return {
        comments: {},
        dataTypes: {},
        dataTypeDocumentations: {},
        elements: {},
        functions: {},
        sequences: {},
        services: {},
        xsd: {},
        xsdAnalysis: null,
        xsdDeltas: [],
        xsdSchema: schema,
    };
}

function updateDictonary<T extends IdAble>(dict: Dictionary<T>, item: Update<T>): Dictionary<T> {
    // Get around TypeScript#12759
    const d: any = dict;
    const it: any = item;

    return {
        ...d,
        [it.id]: {
            ...(d[it.id]),
            ...it,
        },
    };
}

export default function(
    state: ProjectData = createDefault(),
    action: Action,
): ProjectData {
    switch (action.type) {
        case Types.AddComment:
        case Types.UpdateComment:
            const cmts = {
                ...state.comments,
                [action.payload.itemId]: {
                    ...(state.comments[action.payload.itemId]),
                    [action.payload.id]: action.payload,
                },
            };
            return {
                ...state,
                comments: cmts,
            };
        case Types.RemoveComment:
            return {
                ...state,
                comments: mapValues(
                    state.comments,
                    val => (action.payload in val)
                        ? omit(val, action.payload) as any
                        : val,
                ),
            };
        case Types.AddDataType:
        case Types.UpdateDataType:
            return {
                ...state,
                dataTypes: updateDictonary(state.dataTypes, action.payload),
            };
        case Types.RemoveDataType:
            return {
                ...state,
                dataTypes: omit(state.dataTypes, action.payload),
            };
        case Types.AddDataTypeDocumentation:
        case Types.UpdateDataTypeDocumentation:
            return {
                ...state,
                dataTypeDocumentations: updateDictonary(state.dataTypeDocumentations, action.payload),
            };
        case Types.AddElement:
        case Types.UpdateElement:
            return {
                ...state,
                elements: updateDictonary(state.elements, action.payload),
            };
        case Types.RemoveElement:
            return {
                ...state,
                elements: omit(state.elements, action.payload),
            };
        case Types.AddFunction:
        case Types.UpdateFunction:
            return {
                ...state,
                functions: updateDictonary(state.functions, action.payload),
            };
        case Types.RemoveFunction:
            return {
                ...state,
                functions: omit(state.functions, action.payload),
            };
        case Types.AddSequence:
        case Types.UpdateSequence:
            return {
                ...state,
                sequences: updateDictonary(state.sequences, action.payload),
            };
        case Types.RemoveSequence:
            return {
                ...state,
                sequences: omit(state.sequences, action.payload),
            };
        case Types.AddService:
        case Types.UpdateService:
            return {
                ...state,
                services: updateDictonary(state.services, action.payload),
            };
        case Types.RemoveService:
            return {
                ...state,
                services: omit(state.services, action.payload),
            };
        case Types.AddXsdDeltas:
            const { index, values } = action.payload;

            // Are we appending or inserting the deltas?
            if (index === state.xsdDeltas.length) {
                // If we're appending, just apply the delta to the existing tree
                return {
                    ...state,
                    xsd: applyDelta(state.xsd || {}, action.payload),
                    xsdDeltas: state.xsdDeltas.concat(values),
                };
            }

            // If we're inserting, recalculate the entire tree to archive consistent results.
            // This is more expensive (O(n)) than the case above (which is O(1)), but will only
            // happen in case of conflicts.
            console.log("Taking slow XSD path");
            const newDeltas = state.xsdDeltas.slice();
            newDeltas.splice(index, 0, ...values);
            return {
                ...state,
                xsd: applyDelta({}, {
                    index: 0,
                    length: newDeltas.length,
                    values: newDeltas,
                }),
                xsdDeltas: newDeltas,
            };
        case Types.RemoveXsdDeltas:
            console.warn("Unsupported operation! XSD delta array is append-only.");
            return state;
        case Types.UpdateSchemaAnalysis:
            return {
                ...state,
                xsdAnalysis: action.payload,
            };
        case Types.CloseProject_Finish:
            return createDefault();
        default:
            return state;
    }
}
