import { le } from 'binary-search-bounds';
import { Dictionary } from 'lodash';
import values from 'lodash-es/values';
import { XsdElement } from 'omp-schema';
import { xsdRootElementSelector, xsdToString } from 'omp-schema/xsd';
import { ThunkAction } from 'redux-thunk';
import * as Stringifier from 'xmlbuilder/lib/XMLStringifier';

import { Action, PayloadAction, Types } from '../..';
import { API_BACKEND_URL, JSON_MIME } from '../../../config';
import { currentProjectRevisionSelector } from '../../../selectors/project';
import { State, ValidationError } from '../../../state';

export type ValidationActions =
    | LoadValidationFailAction
    | LoadValidationFinishAction
    | LoadValidationStartAction;

export interface LoadValidationFailAction extends PayloadAction<Error> {
    type: Types.LoadValidation_Fail;
    error: true;
}

export interface LoadValidationFinishAction extends PayloadAction<Dictionary<ValidationError[]>> {
    type: Types.LoadValidation_Finish;
}

export interface LoadValidationStartAction {
    type: Types.LoadValidation_Start;
}

interface RowColumnResult {
    /**
     * The starting and ending positions of attributes by corresponding element
     * and attribute ID.
     */
    attributes: Map<string, [number, number, string][]>;

    /**
     * The starting line and outer length of all elements.
     */
    elements: [number, number, string][];
}

export function validateXsd(): ThunkAction<Promise<void>, State, void, Action> {
    return async (dispatch, getState) => {
        dispatch({ type: Types.LoadValidation_Start } as LoadValidationStartAction);

        const state = getState();
        const projectRevision = currentProjectRevisionSelector(state);
        if (!projectRevision) {
            throw new Error("Missing project!");
        }
        const { projectData } = state;
        const xsd = xsdToString(
            projectData.xsd,
            projectData.xsdAnalysis ? projectData.xsdAnalysis.determineXsdPrefixIncludingColon() : '',
            projectRevision.baseUri,
        );

        const baseURI = projectRevision.baseUri || undefined;

        let errors: ValidationError[];
        try {
            errors = await doValidation(xsd, baseURI);
        } catch (err) {
            dispatch({
                type: Types.LoadValidation_Fail,
                error: true,
                payload: err,
            } as LoadValidationFailAction);
            return;
        }

        if (!errors.length) {
            dispatch({
                type: Types.LoadValidation_Finish,
                payload: {},
            } as LoadValidationFinishAction);
        }

        const result: Dictionary<ValidationError[]> = validateDuplicateAttributes(projectData.xsd);

        function pushError(id: string, err: ValidationError) {
            if (!result[id]) {
                result[id] = [] as ValidationError[];
            }
            result[id].push(err);
        }

        const li = calculateLineInformation(projectData.xsd);
        const startElementLines = li.elements.map(([line]) => line);
        for (const err of errors) {
            const idx = le(startElementLines, err.line);
            if (idx === -1) {
                continue;
            }
            const [_start, _end, id] = li.elements[idx];
            const attrs = li.attributes.get(id);
            if (attrs) {
                const attrStartColumns = attrs.map(([start]) => start);
                const attrIdx = le(attrStartColumns, err.column);
                if (attrIdx !== -1) {
                    const [start, len, id] = attrs[attrIdx];
                    if (start + len >= err.column) {
                        pushError(id, err);
                        continue;
                    }
                }
            }

            pushError(id, err);
        }

        dispatch({
            type: Types.LoadValidation_Finish,
            payload: result,
        } as LoadValidationFinishAction);
    };
}

const stringifier = new Stringifier();

function calculateLineInformation(tree: Dictionary<XsdElement>): RowColumnResult {
    const root = xsdRootElementSelector(tree);
    if (!root) {
        throw new Error("Missing root element.");
    }

    // First line is always <?xml> header
    const [res] = calculateElementPositions(tree, root, 2, 1, 2);
    return res;
}

function calculateElementPositions(
    tree: Dictionary<XsdElement>,
    el: XsdElement,
    baseRow: number,
    baseColumn: number,
    indent: number,
): [RowColumnResult, number] {
    const result: RowColumnResult = {
        attributes: new Map(),
        elements: [],
    };

    const attributes = values(el.attributes)
        .sort((a, b) => a.position - b.position);
    let curAttrPos = baseColumn + el.name.length + 1; // Element Name + <

    // Get the map for the corresponding element
    let attrMap = result.attributes.get(el.id);
    if (!attrMap) {
        attrMap = [];
        result.attributes.set(el.id, attrMap);
    }
    for (const attr of attributes) {
        // Attribute name + value + ="" + leading spacing
        const length = attr.name.length + stringifier.attEscape(attr.value).length + 4;
        attrMap.push([curAttrPos, length, attr.id]);
        curAttrPos += length;
    }

    if (!el.content) {
        result.elements.push([baseRow, 1, el.id]);
        return [result, 1];
    } else if (typeof el.content === 'string') {
        const newlineCount = countNewlines(el.content);
        result.elements.push([baseRow, newlineCount + 1, el.id]);
        return [result, newlineCount + 1];
    } else {
        let childLength = 0;
        const childEls: [number, number, string][] = [];

        // Recurse through all children
        for (const child of el.content.map(id => tree[id]).filter(el => el)) {
            const [res, length] = calculateElementPositions(
                tree,
                child,
                baseRow + childLength + 1,
                baseColumn + indent,
                indent,
            );
            childLength += length;
            for (const [k, v] of res.attributes.entries()) {
                result.attributes.set(k, v);
            }

            // Push into a temporary array first to preserve order
            childEls.push(...res.elements);
        }

        // Insert current element first to preserve order, then insert child elements
        result.elements.push([baseRow, childLength + 2, el.id]);
        result.elements.push(...childEls);

        return [result, childLength + 2]; // Start and end nodes
    }
}

function countNewlines(str: string): number {
    return [...str].filter(ch => ch === '\n').length;
}

async function doValidation(xsd: string, baseURI: string | undefined): Promise<ValidationError[]> {
    const resp = await fetch(`${API_BACKEND_URL}/validate`, {
        body: JSON.stringify({ xsd, baseURI }),
        method: 'post',
        headers: {
            'Accept': JSON_MIME,
            'Content-Type': JSON_MIME,
        },
    });
    const body = await resp.json();
    return body.success || !body.errors ? [] : body.errors;
}

function validateDuplicateAttributes(xsd: Dictionary<XsdElement>): Dictionary<ValidationError[]> {
    const result = {};

    values(xsd).forEach(element => {
        const attributes = values(element.attributes);
        const counted = attributes.reduce(
            (store, attr) => Object.assign(store, { [attr.name]: (store[attr.name] || 0) + 1 }),
            {},
        );

        Object.keys(counted)
            .filter(name => counted[name] > 1)
            .forEach(name => {
                attributes.filter(attr => attr.name === name)
                    .forEach(attr => result[attr.id] = [{
                        column: 0,
                        line: 0,
                        // tslint:disable-next-line:max-line-length
                        message: `Duplicate attribute '${name}'. This will lead to unexpected results in the final XSD.`,
                    }]);
            });
    });

    return result;
}
