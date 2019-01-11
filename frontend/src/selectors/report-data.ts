import Quill from 'quill';
import { createSelector } from 'reselect';

// Note: import from yjs module here because the logic in here is yjs specific
import { getYProjectRevision } from '../actions/sync/yjs';

import {
    currentProjectRevisionSelector,
    currentProjectSelector,
    dataTypesSelector,
    elementsSelector,
    functionsSelector,
    sequencesSelector,
    servicesSelector,
    xsdAnalysisSelector,
    xsdSelector,
} from './project';

export const generateReportData = createSelector(
    dataTypesSelector,
    elementsSelector,
    functionsSelector,
    sequencesSelector,
    servicesSelector,
    xsdSelector,
    xsdAnalysisSelector,
    currentProjectSelector,
    currentProjectRevisionSelector,
    getYProjectRevision,
    () => {}, // TODO: Add real protobuf generation code here
);

let quillElt: HTMLElement;
let quill: Quill;
let xhtmlDoc: Document;

function richtextToString(richText: any): string {
    if (!quill) {
        quillElt = document.createElement('div');
        quill = new Quill(quillElt);

        xhtmlDoc = document.implementation.createDocument(
            'http://www.w3.org/1999/xhtml',
            'html',
            null,
        );
    }

    quill.setContents(richText.toDelta());
    const el = quillElt.querySelector('.ql-editor')!;
    return xhtmlDoc.importNode(el, true).innerHTML;
}
