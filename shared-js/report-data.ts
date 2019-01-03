import {
    values,
    DataType,
    DataTypeDocumentation,
    Documentable,
    Element as Elt,
    Function as Fn,
    Language,
    Project,
    ProjectRevision,
    Sequence,
    Service,
    XsdElement,
} from '.';
//import { OMPInterfaceToolProjectSchema } from './backend';
import { xsdToString } from './xsd';
import { SchemaAnalysis } from './xsd-analysis';

/*
export const makeGenerateReportData = (richtextToString: (yRichtext: any) => string) => (
    dataTypes: Record<string, DataType>,
    elements: Record<string, Elt>,
    functions: Record<string, Fn>,
    sequences: Record<string, Sequence>,
    services: Record<string, Service>,
    xsd: Record<string, XsdElement>,
    xsdAnalysis: SchemaAnalysis | null,
    project: Project | null,
    projectRevision: ProjectRevision | null,
    yProjectRevision: any,
) => {
    if (!yProjectRevision) {
        throw new Error("Missing y project!");
    }
    if (!project || !projectRevision) {
        throw new Error("Missing project or project revision!");
    }

    const genDefault = (item: Documentable & { name: string }) => ({
        name: item.name,
        documentation: {
            german: richtextToString(yProjectRevision.share.docTexts.get(item.docText[Language.German])),
            english: richtextToString(yProjectRevision.share.docTexts.get(item.docText[Language.English])),
        },
    });

    return {
        project: {
            title: project.name,
            documentation: {
                german: richtextToString(
                    yProjectRevision.share.docTexts.get(projectRevision.docText[Language.German])
                ),
                english: richtextToString(
                    yProjectRevision.share.docTexts.get(projectRevision.docText[Language.English])
                ),
            },
        },
        elements: values(elements).map(el => ({
            ncname: el.name,
            documentation: {
                english: '<p xmlns="http://www.w3.org/1999/xhtml"><br /></p>',
                german: '<p xmlns="http://www.w3.org/1999/xhtml"><br /></p>',
            },
        })),
        datatypes: values(dataTypes).map(dt => {
            const docObj: DataTypeDocumentation = yProjectRevision.share.dataTypeDocumentations.get(dt.id);
            return {
                ncname: dt.name,
                documentation: {
                    german: richtextToString(
                        yProjectRevision.share.docTexts.get(docObj.docText[Language.German])
                    ),
                    english: richtextToString(
                        yProjectRevision.share.docTexts.get(docObj.docText[Language.English])
                    ),
                },
            };
        }),
        functions: values(functions).map(fn => ({
            ncname: fn.name,
            documentation: {
                german: richtextToString(yProjectRevision.share.docTexts.get(fn.docText[Language.German])),
                english: richtextToString(yProjectRevision.share.docTexts.get(fn.docText[Language.English])),
            },
            inputElementName: elements[fn.request] && {
                namespaceuri: (xsdAnalysis && xsdAnalysis.targetNamespace) || '',
                ncname: elements[fn.request].name,
            },
            outputElementName: elements[fn.response] && {
                namespaceuri: (xsdAnalysis && xsdAnalysis.targetNamespace) || '',
                ncname: elements[fn.response].name,
            },
            assertions: fn.assertions.map(a => ({ test: a })),
        })),
        sequences: values(sequences).map(genDefault),
        services: values(services).map(sv => ({
            ...genDefault(sv),
            functions: sv.functions.map(fnId => functions[fnId].name),
            sequences: sv.sequences.map(sqId => sequences[sqId].name),
        })),
        schema: {
            xsd: xsdToString(
                xsd,
                xsdAnalysis ? xsdAnalysis.determineXsdPrefixIncludingColon() : '',
                projectRevision.baseUri
            ),
            baseURI: projectRevision.baseUri || undefined,
        }
    } as OMPInterfaceToolProjectSchema;
};
 */