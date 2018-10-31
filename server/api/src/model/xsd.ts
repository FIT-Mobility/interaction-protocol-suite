import { JSDOM } from 'jsdom';
import {
    DataType,
    Element,
    Function as Fn,
    Project,
    ProjectRevision,
    PROJECT_ROOM,
    Sequence,
    Service,
    XsdAttribute,
    XsdElement,
} from 'omp-schema/commonjs';
import { makeGenerateReportData } from 'omp-schema/commonjs/report-data';
import { ElementTypes } from 'omp-schema/commonjs/schema-tree';
import { applyDelta, xsdToString } from 'omp-schema/commonjs/xsd';
import {
    DataTypeAnalysis,
    ElementAnalysis,
    SchemaAnalysis,
    XsdAnalyzer,
} from 'omp-schema/commonjs/xsd-analysis';

import { getY } from '../yjs';

const analyzer = new XsdAnalyzer([
    ElementTypes.XS_ComplexType,
    ElementTypes.XS_Element,
    ElementTypes.XS_SimpleType,
    ElementTypes.XS_Schema,
]);

let loadPromise: Promise<void>;
const richtextToXhtml = (() => { // Separate scope for brevity
    const dom = new JSDOM(`
        <script src="https://cdnjs.cloudflare.com/ajax/libs/webcomponentsjs/0.7.22/MutationObserver.js"></script>
        <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script>

        <div id="editor-container"></div>
    `, {
        resources: 'usable',
        runScripts: 'dangerously',
    });

    dom.window.document.getSelection = () => ({ getRangeAt: () => {} } as any);
    dom.window.document.execCommand = () => false;

    loadPromise = new Promise(res => dom.window.addEventListener('load', () => res()));

    return (yRichtext) => {
        const container = dom.window.document.createElement("div");
        dom.window.document.body.appendChild(container);
        const quill = new (dom.window as any).Quill(container, {});
        quill.setContents(yRichtext.toDelta());

        const html = container.querySelector(".ql-editor")!.innerHTML;
        container.remove();

        return html;
    };
})();

const generateReportData = makeGenerateReportData(richtextToXhtml);

const yMapToObj = <T>(yMap): Record<string, T> => {
    return yMap.keys().reduce((acc, k) => {
        acc[k] = yMap.get(k);
        return acc;
    }, {} as Record<string, T>);
};

export async function getProjectData(projectRevisionId: string) {
    const [yProjectsRoom, yProj] = await Promise.all([
        getY(PROJECT_ROOM),
        getY(projectRevisionId),
        loadPromise,
    ]);
    if (!yProj) {
        return null;
    }
    const projectRevision: ProjectRevision | null =
        yProjectsRoom.share.projectRevisions.get(projectRevisionId);
    if (!projectRevision) {
        return null;
    }
    const project: Project | null =
        yProjectsRoom.share.projects.get(projectRevision.project);
    if (!project) {
        return null;
    }

    // Now extract the project's data out of yjs

    const data = yProj.share.xsd.toArray();
    const xsdTree: Record<string, XsdElement> = applyDelta({}, {
        index: 0,
        length: data.length,
        values: data,
    });

    analyzer.reset();
    const analysis = analyzer.analyze(xsdTree);

    const elements: Record<string, Element> = {};
    if (analysis[ElementTypes.XS_Element]) { // There may be no elements
        analysis[ElementTypes.XS_Element].added.filter((an) => {
            return (an as ElementAnalysis).isTopLevel;
        }).reduce((acc, an) => {
            const eltAn = an as ElementAnalysis;
            acc[eltAn.element.id] = {
                createdOn: eltAn.createdOn,
                id: eltAn.element.id,
                isTopLevel: eltAn.isTopLevel,
                name: eltAn.name!,
                type: eltAn.type!,
            };
            return acc;
        }, elements);
    }

    const dataTypes: Record<string, DataType> = {};
    const dtReducer = (acc, an) => {
        const dtAn = an as DataTypeAnalysis;
        acc[an.element.id] = {
            createdOn: dtAn.createdOn,
            id: dtAn.element.id,
            isComplex: dtAn.isComplex,
            name: dtAn.name!,
        };
        return acc;
    };
    if (analysis[ElementTypes.XS_ComplexType]) { // There may be no complex types
        analysis[ElementTypes.XS_ComplexType].added.reduce(dtReducer, dataTypes);
    }
    if (analysis[ElementTypes.XS_SimpleType]) { // There may be no simple types
        analysis[ElementTypes.XS_SimpleType].added.reduce(dtReducer, dataTypes);
    }

    const functions = yMapToObj<Fn>(yProj.share.functions);
    const sequences = yMapToObj<Sequence>(yProj.share.sequences);
    const services = yMapToObj<Service>(yProj.share.services);

    return generateReportData(
        dataTypes,
        elements,
        functions,
        sequences,
        services,
        xsdTree,
        analysis[ElementTypes.XS_Schema].added[0] as SchemaAnalysis,
        project,
        projectRevision,
        yProj,
    );
}

/**
 * Gets the XSD from a project revision as string.
 *
 * @param projectRevisionId the ID project revision to get the XSD from
 */
export async function getXsdString(projectRevisionId: string): Promise<string | null> {
    const [yProjectsRoom, yProj] = await Promise.all([
        getY(PROJECT_ROOM),
        getY(projectRevisionId),
    ]);
    if (!yProj) {
        return null;
    }
    const projectRevision: ProjectRevision = yProjectsRoom.share.projectRevisions.get(projectRevisionId);

    const data = yProj.share.xsd.toArray();
    const tree: Record<string, XsdElement> = applyDelta({}, {
        index: 0,
        length: data.length,
        values: data,
    });

    const root = Object.keys(tree)
        .map(k => tree[k])
        .find(el => !el.parentId);
    if (!root) {
        throw new Error("Missing root element");
    }

    /**
     * Determines the prefix (including the colon) currently used for the XML-Schema-Namespace in the schema
     * (or the empty string in case no prefix is used).
     *
     * @param {Record<string, XsdAttribute>)} attributes the root element attributes
     * @returns {string} the prefix (including colon) or the empty string
     */
    function determineXsdPrefixIncludingColon(attributes: Record<string, XsdAttribute>): string {
        const xsdnsAttrs: XsdAttribute[] = Object.keys(attributes)
            .map(k => attributes[k])
            .filter(attr => attr.value === 'http://www.w3.org/2001/XMLSchema')
            .filter(attr => attr.name.startsWith('xmlns'))
            .sort((a, b) =>  a.name.localeCompare(b.name));
        const prefix: XsdAttribute | undefined = xsdnsAttrs[0];
        if (prefix === undefined || prefix.name === 'xmlns') {
            return '';
        }
        return prefix.name.split(':')[1] + ':';
    }

    const xsdString: string = xsdToString(
        tree,
        determineXsdPrefixIncludingColon(root.attributes),
        projectRevision.baseUri,
    );

    return xsdString;
}
