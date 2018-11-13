import { values, XsdAttribute, XsdElement } from '.';
import schemaTree, { AttributeTypes, ElementTypes } from './schema-tree';
import { xsdRootElementSelector } from './xsd';

export interface XsdAnalysis {
    createdOn: number;
    element: XsdElement;
    name: string | null;
}

export interface DataTypeAnalysis extends XsdAnalysis {
    isComplex: boolean;
}

export interface ElementAnalysis extends XsdAnalysis {
    isTopLevel: boolean;
    type: string | null;
}

export class SchemaAnalysis implements XsdAnalysis {
    constructor(
        public createdOn: number,
        public element: XsdElement,
        public name: string | null,
        public namespaces: {
            [prefix: string]: string;
        },
        public targetNamespace: string,
        public xmlns: string,
    ) { }

    determineXsdPrefixIncludingColon() {
        if (this.xmlns === 'http://www.w3.org/2001/XMLSchema') {
            return '';
        }
        const prefix = Object.keys(this.namespaces)
            .find(key => this.namespaces[key] === 'http://www.w3.org/2001/XMLSchema');
        return prefix ? prefix + ':' : '';
    }
}

export interface SchemaAnalysis extends XsdAnalysis {
    namespaces: {
        [prefix: string]: string;
    };
    targetNamespace: string;
    xmlns: string;
}

export interface Analysis<T extends XsdAnalysis> {
    added: T[];
    removed: T[];
    updated: T[];
}

export interface AnalysisResult {
    [tagName: string]: Analysis<XsdAnalysis>;
}

/**
 * The XSD analyzer, providing intelligence about an XSD tree.
 */
export class XsdAnalyzer {
    private static analyzeElement(root: XsdElement | null, element: XsdElement): XsdAnalysis {
        let analysis: XsdAnalysis = {
            createdOn: element.createdOn,
            element,
            name: XsdAnalyzer.attrOfName(element.attributes, 'name'),
        };

        switch (element.type) {
            case ElementTypes.XS_Element:
                if (!root) {
                    throw new Error("Missing root element.");
                }
                const elAn: ElementAnalysis = {
                    ...analysis,
                    isTopLevel: element.parentId === root.id,
                    type: XsdAnalyzer.attrOfName(
                        element.attributes,
                        schemaTree.attributes[AttributeTypes.Type].name,
                    ),
                };
                analysis = elAn;
                break;

            case ElementTypes.XS_ComplexType:
            case ElementTypes.XS_SimpleType:
                const dtAn: DataTypeAnalysis = {
                    ...analysis,
                    isComplex: element.type === ElementTypes.XS_ComplexType,
                };
                analysis = dtAn;
                break;

            case ElementTypes.XS_Schema:
                const nsp: Record<string, string> = {};
                values(element.attributes)
                    .filter(attr => attr.name.startsWith('xmlns:'))
                    .forEach(attr => nsp[attr.name.split(':')[1]] = attr.value);

                analysis = new SchemaAnalysis(
                    analysis.createdOn,
                    analysis.element,
                    analysis.name,
                    nsp,
                    XsdAnalyzer.attrOfName(
                        element.attributes,
                        schemaTree.attributes[AttributeTypes.TargetNameSpace].name,
                    ) || '',
                    XsdAnalyzer.attrOfName(element.attributes, 'xmlns') || '',
                );
                break;
        }

        return analysis;
    }

    /**
     * Determines whether the analysis of a node has changed.
     *
     * @param {XsdAnalysis} prev the previous analysis.
     * @param {XsdAnalysis} cur the current analysis.
     * @returns {boolean} whether there have been changes to the XSD node between taking the analysis.
     */
    private static hasUpdated(prev: XsdAnalysis, cur: XsdAnalysis): boolean {
        if (prev.element.name !== cur.element.name) {
            throw new Error("Trying to diff elements of different types.");
        }

        switch (prev.element.name) {
            case ElementTypes.XS_Element:
                const x = prev as ElementAnalysis;
                const y = cur as ElementAnalysis;
                return x.name !== y.name || x.type !== y.type;
            case ElementTypes.XS_ComplexType:
            case ElementTypes.XS_SimpleType:
                const a = prev as DataTypeAnalysis;
                const b = cur as DataTypeAnalysis;
                return a.isComplex !== b.isComplex || a.name !== b.name;
        }

        return false;
    }

    /**
     * Returns the value of the attribute with the given name.
     *
     * @param {Record<string, XsdAttribute>} attributes the set of attributes on the element
     * @param {string} name the name of the attribute so search for
     * @returns {string | null} the value of the attribute or null, if there was no attribute with the given name
     */
    private static attrOfName(attributes: Record<string, XsdAttribute>, name: string): string | null {
        const attr = values(attributes)
            .find(attr => attr.name === name);
        return attr ? attr.value : null;
    }

    private readonly elementTypes: Set<string>;
    private prevAnalysis: Record<string, XsdAnalysis> | undefined;
    private prevKeys: Set<string> = new Set<string>();

    constructor(elementTypes: string[]) {
        this.elementTypes = new Set(elementTypes);
    }

    /**
     * Analyzes the given XSD tree and caches the result for diffing with subsequent analyses.
     *
     * @param {Record<string, XsdElement>} xsd the XSD tree
     * @returns {AnalysisResult} the result of the analysis
     */
    analyze(xsd: Record<string, XsdElement>): AnalysisResult {
        return this.diffAnalyses(this.extractAnalysis(xsd));
    }

    /**
     * Resets the analyser so that subsequent analyses are treated as if there were no
     * previous ones.
     */
    reset() {
        this.prevAnalysis = undefined;
        this.prevKeys.clear();
    }

    /**
     * Diffs the currently stored analysis against one that has been created by {@ref extractAnalysis}.
     *
     * @param {module:../index.Record<string, XsdAnalysis>} vsNew the new analysis.
     * @returns {AnalysisResult} The difference between the two.
     */
    protected diffAnalyses(vsNew: Record<string, XsdAnalysis>): AnalysisResult {
        // This is the first analysis we run, simply return all elements as added to
        // the set and cache the analysis.
        if (!this.prevAnalysis) {
            this.prevAnalysis = vsNew;
            this.prevKeys = new Set(Object.keys(vsNew));

            return values(vsNew).reduce((acc, it) => {
                if (!acc[it.element.type]) {
                    acc[it.element.type] = { added: [], removed: [], updated: [] };
                }
                acc[it.element.type].added.push(it);
                return acc;
            }, {} as AnalysisResult);
        }

        const result: AnalysisResult = {};
        const newKeys = new Set(Object.keys(vsNew));

        // TODO: Abstract this
        for (const removedKey of [...this.prevKeys].filter(k => !newKeys.has(k))) {
            const an = this.prevAnalysis[removedKey];
            if (!result[an.element.type]) {
                result[an.element.type] = { added: [], removed: [], updated: [] };
            }
            result[an.element.type].removed.push(an);
        }
        for (const addedKey of [...newKeys].filter(k => !this.prevKeys.has(k))) {
            const an = vsNew[addedKey];
            if (!result[an.element.type]) {
                result[an.element.type] = { added: [], removed: [], updated: [] };
            }
            result[an.element.type].added.push(an);
        }
        for (const maybeUpdatedKey of [...newKeys].filter(k => this.prevKeys.has(k))) {
            const prev = this.prevAnalysis[maybeUpdatedKey];
            const cur = vsNew[maybeUpdatedKey];
            if (!XsdAnalyzer.hasUpdated(prev, cur)) {
                continue;
            }
            if (!result[prev.element.type]) {
                result[prev.element.type] = { added: [], removed: [], updated: [] };
            }
            result[prev.element.type].updated.push(cur);
        }

        this.prevAnalysis = vsNew;
        this.prevKeys = newKeys;

        return result;
    }

    protected extractAnalysis(xsd: Record<string, XsdElement>): Record<string, XsdAnalysis> {
        const root = xsdRootElementSelector(xsd);
        return values(xsd)
            .filter(el => this.elementTypes.has(el.name))
            .reduce((acc, el) => {
                acc[el.id] = XsdAnalyzer.analyzeElement(root, el);
                return acc;
            }, {} as Record<string, XsdAnalysis>);
    }
}
