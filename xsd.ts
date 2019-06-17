import { create, XMLElementOrXMLNode } from 'xmlbuilder';

import {
    ArrayDelta,
    EditAttributeNameXsdOperation,
    EditAttributeValueXsdOperation,
    XsdAttribute,
    XsdElement,
    XsdOperation,
    XsdOperationType,
} from '.';
import schemaTree, { AttributeTypes, ElementTypes } from './schema-tree';

/**
 * Gets the root element of the given XSD document.
 *
 * @param xsd the XSD document
 * @returns {XsdElement | null} the root XSD or null, if there exists none.
 */
export const xsdRootElementSelector = (xsd: Record<string, XsdElement>): XsdElement | null => {
    const key = Object.keys(xsd).find(k => !xsd[k].parentId);
    return key ? xsd[key] : null;
};

/**
 * Applies the given XSD operation array delta to the XSD tree state.
 *
 * This method must only be called in response to additions of XSD delta operations,
 * it cannot process removal of XSD delta operations.
 *
 * @param {Record<string, Record<string, XsdElement>>} xsd the XSD tree state.
 * @param {ArrayDelta<XsdOperation>} delta the delta to apply to the XSD tree state.
 * @returns {Record<string, Record<string, XsdElement>>} the new XSD tree state.
 */
export function applyDelta(
    xsd: Record<string, XsdElement>,
    delta: ArrayDelta<XsdOperation>,
): Record<string, XsdElement> {
    return delta.values.reduce((xsd, op, idx) => {
        switch (op.type) {
            case XsdOperationType.AddAttribute:
                const el = xsd[op.elementId];

                if (!el) {
                    console.warn("Missing element to add attribute to.");
                    return xsd;
                }
                if (Object.keys(el.attributes).some(k => el.attributes[k].name === op.name)) {
                    /* tslint:disable-next-line:max-line-length */
                    console.warn(`Attribute '${op.name}' is already present on element <${el.name}/> (ID: ${el.id}).`);
                }

                return editElement(xsd, op.elementId, el => {
                    // Update the positions of the rest of the attributes
                    const attrsWithUpdatedPositions = Object.keys(el.attributes)
                        .reduce((acc, key) => {
                            const attr = el.attributes[key];
                            acc[key] = {
                                ...attr,
                                position: attr.position < op.position
                                    ? attr.position
                                    : attr.position + 1,
                            };
                            return acc;
                        }, {} as Record<string, XsdAttribute>);

                    return {
                        ...el,
                        attributes: {
                            ...attrsWithUpdatedPositions,
                            [op.attributeId]: {
                                createdOn: op.createdOn,
                                id: op.attributeId,
                                name: op.name,
                                position: op.position,
                                value: op.value,
                            },
                        },
                    };
                });

            case XsdOperationType.AddNode:
                if (op.elementId === op.parentId) {
                    console.warn("Unsupported operation, circular tree reference!");
                    return xsd;
                }
                if (!op.parentId && Object.keys(xsd).some(k => !xsd[k].parentId)) {
                    console.warn("Unsupported operation, cannot have two root elements.");
                    return xsd;
                }

                const newElt: XsdElement = {
                    attributes: {},
                    content: null,
                    createdOn: op.createdOn,
                    id: op.elementId,
                    name: op.name,
                    parentId: op.parentId,
                    type: op.elementType,
                };

                if (op.parentId) {
                    const parent = xsd[op.parentId];

                    if (!parent) {
                        console.warn("Missing parent of element to insert under.");
                        return xsd;
                    }

                    // Register child within parent
                    const content = Array.isArray(parent.content)
                        ? parent.content.slice() // Since our data is immutable, we make a flat copy with .slice()
                        : [];
                    content.splice(op.position, 0, op.elementId);

                    return {
                        ...xsd,
                        [op.elementId]: newElt,
                        [op.parentId]: {
                            ...parent,
                            content,
                        },
                    };
                } else {
                    return {
                        ...xsd,
                        [op.elementId]: newElt,
                    };
                }

            case XsdOperationType.EditAttribute:
                return editElement(xsd, op.elementId, el => {
                    if (!(op.attributeId in el.attributes)) {
                        console.warn("Unsupported operation, editing nonexistent attribute.");
                        return el;
                    }

                    const attr = el.attributes[op.attributeId];
                    return {
                        ...el,
                        attributes: {
                            ...el.attributes,
                            [op.attributeId]: {
                                ...(el.attributes[op.attributeId]),

                                // The XSD operation updates either the name or the value.
                                name: (op as EditAttributeNameXsdOperation).name !== undefined
                                    ? (op as EditAttributeNameXsdOperation).name
                                    : attr.name,
                                value: (op as EditAttributeValueXsdOperation).value !== undefined
                                    ? (op as EditAttributeValueXsdOperation).value
                                    : attr.value,
                            },
                        },
                    };
                });

            case XsdOperationType.EditTextContent:
                return editElement(xsd, op.elementId, el => ({ ...el, content: op.newContent }));

            case XsdOperationType.MoveNode:
                const element = xsd[op.elementId];
                if (!element) {
                    console.warn("Moving nonexistent element.");
                    return xsd;
                }
                if (!element.parentId) {
                    console.warn("Unsupported operation, moving root element.");
                    return xsd;
                }

                return editElement(xsd, element.parentId, el => {
                    if (!el.content || typeof el.content === 'string') {
                        console.warn("Unsupported operation, moving childs within element with string content.");
                        return el;
                    }

                    // Take all current children except the element to be moved
                    // performing a flat copy
                    const children = el.content.filter(elId => elId !== op.elementId);
                    // And insert the children at the right position
                    children.splice(op.newPosition, 0, op.elementId);

                    return {
                        ...el,
                        content: children,
                    };
                });

            case XsdOperationType.RemoveAttribute:
                return editElement(xsd, op.elementId, el => {
                    // Remove relevant attribute and update the positions of the rest accordingly

                    let idx = 0;
                    const newAttrs = Object.keys(el.attributes).reduce((acc, key) => {
                        if (key === op.attributeId) {
                            return acc;
                        }

                        acc[key] = {
                            ...(el.attributes[key]),
                            position: idx++,
                        };
                        return acc;
                    }, {} as Record<string, XsdAttribute>);
                    return {
                        ...el,
                        attributes: newAttrs,
                    };
                });

            case XsdOperationType.RemoveNode:
                const elt = xsd[op.elementId];

                if (!elt) {
                    console.warn("Removing non-existent XSD element.");
                    return xsd;
                }
                if (!elt.parentId) {
                    console.warn("Unsupported operation, cannot remove root element.");
                    return xsd;
                }

                const nodesToRemove = new Set<string>();
                collectNodesToRemove(xsd, op.elementId, nodesToRemove);

                const newXsd: Record<string, XsdElement> = {};
                Object.keys(xsd)
                    .filter(elId => !nodesToRemove.has(elId))
                    .forEach(elId => newXsd[elId] = xsd[elId]);

                if (elt.parentId) {
                    const parent = newXsd[elt.parentId];
                    newXsd[elt.parentId] = {
                        ...parent,
                        content: Array.isArray(parent.content)
                            ? parent.content.filter(id => id !== op.elementId)
                            : parent.content,
                    };
                }

                return newXsd;
        }
    }, xsd);
}

/* tslint:disable:max-line-length */

/**
 * Edits the given XSD element inside the global XSD element tree.
 *
 * @param {XsdElement} xsd the global XSD state.
 * @param {string} elId the ID of the XSD element to edit.
 * @param {(r: Record<string, XsdElement>) => Record<string, XsdElement>} callback the callback to process the XSD element with.
 * @returns {Record<string, Record<string, XsdElement>>} the new, shallowly-copied state.
 */
function editElement(
    xsd: Record<string, XsdElement>,
    elId: string,
    callback: (r: XsdElement) => XsdElement,
): Record<string, XsdElement> {
    const el = xsd[elId];
    if (!el) {
        console.warn("Missing element to edit.");
        return xsd;
    }

    return {
        ...xsd,
        [elId]: callback(el),
    };
}

/* tslint:enable */

/**
 * Transitively collects the child nodes to be removed when a node is supposed
 * to be removed from the XSD tree.
 *
 * @param {XsdElement} xsd the global XSD state.
 * @param elToRemove
 * @param result
 */
function collectNodesToRemove(
    xsd: Record<string, XsdElement>,
    elToRemove: string,
    result: Set<string>,
) {
    result.add(elToRemove);

    const el = xsd[elToRemove];
    if (!el || !Array.isArray(el.content)) {
        return;
    }

    for (const childId of el.content) {
        collectNodesToRemove(xsd, childId, result);
    }
}

/* tslint:disable:max-line-length */
/**
 * Converts the given XSD tree into the real XSD string representation.
 *
 * @param {Record<string, XsdElement>} xsd the XSD tree.
 * @param {string} xsdPrefixIncludingColon the prefix (including the colon) to be used for the XML-Schema-Namespace (or the empty string in case no prefix is to be used).
 * @param {string} baseUri the base URI of the project revision that is to be used to make all relative references absolute.
 * @returns {string} the XSD tree string representation.
 */
/* tslint:enable */
export function xsdToString(
    xsd: Record<string, XsdElement>,
    xsdPrefixIncludingColon: string,
    baseUri: string | null,
): string {
    /**
     * Replaces relative URIs in schemaLocation attributes of the given XSD element by absolute ones.
     *
     * @param {XsdElement} elt the XSD element to inspect for schemaLocation attributes
     * @returns {Record<string, XsdAttribute>} the processed attributes of the element given
     */
    function resolveImports(elt: XsdElement) : Record<string, XsdAttribute> {
        switch (elt.type) {
            default:
                return elt.attributes;
            case ElementTypes.XS_Include:
            case ElementTypes.XS_Import:
            case ElementTypes.XS_Redefine:
            // case ElementTypes.XS_Override:
                break;
        }

        const schemaLocationKey = schemaTree.attributes[AttributeTypes.SchemaLocation].name;
        const schemaLocation = Object.keys(elt.attributes)
            .map(attr => elt.attributes[attr])
            .find(attr => attr.name === schemaLocationKey);

        if (!schemaLocation || /^(?:[a-z]+:)?\/\//i.test(schemaLocation.value)) {
            return elt.attributes;
        }
        if (!baseUri) {
            console.warn('no base uri specified, but relative import found!');
        }

        return {
            ...elt.attributes,
            [schemaLocationKey]: {
                ...schemaLocation,
                value: (new URL(schemaLocation.value, baseUri || undefined)).toString(),
            },
        };
    }

    /* tslint:disable:max-line-length */
    /**
     * Recursively builds up the xmlbuilder representation of the given XSD element.
     *
     * @param {XsdElement} elt the XSD element to build the representation of.
     * @param {xmlbuilder.XMLElementOrXMLNode} parentNode the xmlbuilder parent node or null, if the given element is the root element of the XSD schema.
     */
    /* tslint:enable */
    function visitElement(elt: XsdElement, parentNode: XMLElementOrXMLNode | null): XMLElementOrXMLNode {
        const attributes = resolveImports(elt);
        const elementNameWithPrefix = xsdPrefixIncludingColon + elt.name;

        const currentNode = parentNode
            ? parentNode.ele(elementNameWithPrefix)
            : create(elementNameWithPrefix);

        Object.keys(attributes)
            .map(k => attributes[k])
            .forEach(attribute => currentNode.att(attribute.name.trim(), attribute.value.trim()));

        if (Array.isArray(elt.content)) {
            for (const childEl of elt.content) {
                visitElement(xsd[childEl], currentNode);
            }
        } else if (typeof elt.content === 'string') {
            currentNode.txt(elt.content);
        }

        return currentNode;
    }

    const root = Object.keys(xsd)
        .map(k => xsd[k])
        .find(el => !el.parentId);
    if (!root) {
        throw new Error("Missing root element");
    }

    return visitElement(root, null).end({ pretty: true, allowEmpty: true });
}
