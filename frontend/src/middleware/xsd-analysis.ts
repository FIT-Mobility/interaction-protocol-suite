/*
 * This redux middleware parses data type information out of arriving XSD deltas.
 */

import values from 'lodash-es/values';
import { DataType, Element } from 'omp-schema';
import { ElementTypes } from 'omp-schema/schema-tree';
import { DataTypeAnalysis, ElementAnalysis, SchemaAnalysis, XsdAnalyzer } from 'omp-schema/xsd-analysis';
import { Store } from 'redux';

import { Action, Types } from '../actions';
import {
    addDataType,
    addElement,
    createDataTypeDocumentationIfNotExists,
    removeDataType,
    removeElement,
    updateDataType,
    updateElement,
    updateFunctionRequestType,
    updateFunctionResponseType,
    updateSchemaAnalysis,
} from '../actions/sync';
import { State } from '../state';

const analyzer = new XsdAnalyzer([
    ElementTypes.XS_ComplexType,
    ElementTypes.XS_Element,
    ElementTypes.XS_SimpleType,
    ElementTypes.XS_Schema,
]);
const analysisToDataType = (an: DataTypeAnalysis): DataType => ({
    createdOn: an.createdOn,
    isComplex: an.isComplex,
    id: an.element.id,
    name: an.name || 'New Data Type',
});
const analysisToElement = (an: ElementAnalysis): Element => ({
    createdOn: an.element.createdOn,
    id: an.element.id,
    isTopLevel: an.isTopLevel,
    name: an.name || 'New Element',
    type: an.type || 'N/A',
});
const dataTypeRedux = {
    add: (an: DataTypeAnalysis) => addDataType(analysisToDataType(an)),
    remove: (an: DataTypeAnalysis) => removeDataType(an.element.id),
    update: (an: DataTypeAnalysis) => updateDataType(analysisToDataType(an)),
};
const reduxMap = {
    [ElementTypes.XS_ComplexType]: dataTypeRedux,
    [ElementTypes.XS_Element]: {
        add: (an: ElementAnalysis) => addElement(analysisToElement(an)),
        remove: (an: ElementAnalysis) => removeElement(an.element.id),
        update: (an: ElementAnalysis) => updateElement(analysisToElement(an)),
    },
    [ElementTypes.XS_Schema]: {
        add: (an: SchemaAnalysis) => updateSchemaAnalysis(an),
        remove: () => {},
        update: (an: SchemaAnalysis) => updateSchemaAnalysis(an),
    },
    [ElementTypes.XS_SimpleType]: dataTypeRedux,
};

export default (store: Store<State, any>) => (next: (ac: any) => any) => (action: Action) => {
    next(action);

    switch (action.type) {
        case Types.AddXsdDeltas:
            break;
        case Types.CloseProject_Finish:
            analyzer.reset();
            return;
        default:
            return;
    }

    const { xsd } = store.getState().projectData;
    const result = analyzer.analyze(xsd);

    for (const elementType of Object.keys(result)) {
        const operationMap = reduxMap[elementType];
        const analysis = result[elementType];
        for (const added of analysis.added) {
            store.dispatch(operationMap.add(added));

            if (elementType === ElementTypes.XS_SimpleType || elementType === ElementTypes.XS_ComplexType) {
                store.dispatch(createDataTypeDocumentationIfNotExists(added.element.id));
            }
        }

        for (const removed of analysis.removed) {
            store.dispatch(operationMap.remove(removed));

            // Fix dangling references to elements
            if (elementType === ElementTypes.XS_Element) {
                const { functions } = store.getState().projectData;
                values(functions).forEach(fn => {
                    if (fn.response === removed.element.id) {
                        store.dispatch(updateFunctionResponseType(fn.id, ''));
                    }
                    if (fn.request === removed.element.id) {
                        store.dispatch(updateFunctionRequestType(fn.id, ''));
                    }
                });
            }
        }

        for (const updated of analysis.updated) {
            store.dispatch(operationMap.update(updated));
        }
    }
};
