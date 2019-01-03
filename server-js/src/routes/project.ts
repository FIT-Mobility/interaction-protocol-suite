import {
    AddAttributeXsdOperation,
    AddNodeXsdOperation,
    EditTextContentXsdOperation,
    XsdOperation,
    XsdOperationType,
} from '@ips/shared-js';
import schemaTree, { ElementTypes } from "@ips/shared-js/schema-tree";
import { Express } from "express";
import { readFile } from 'fs';
import slugify from "slugify";
import { promisify } from 'util';
import { v4 as uuid } from 'uuid';
import { js2xml, xml2js, Element } from 'xml-js';

import { ensureLoggedIn, ensureObjIdValid, ensurePresent, FileModel } from "../model";
import { createProject, isProjectUrlSlugUnique } from "../yjs";

import { handlify } from ".";

const read = promisify(readFile);

/**
 * Converts an XML element into the appropriate delta representation.
 *
 * @param {Element} el the element to convert to XSD deltas
 * @param {ElementTypes[]} childrenToChooseFrom the list of ElementTypes allowed here according to schemaTree
 * @param {string | null} parentId the id of the parent node
 * @param {number} childPos the current position of this node within the list of children of the parent node
 * @returns {XsdOperation[]} a list of deltas that lead to the input
 */
function toDeltas(el: Element, childrenToChooseFrom: ElementTypes[], parentId: string | null, childPos: number)
    : XsdOperation[] {
    console.assert(el.name, `Element ${JSON.stringify(el)} did not have a name.`);

    const name = el.name!.indexOf(':') !== -1
        ? el.name!.split(':')[1]
        : el.name!;
    const now = Date.now();
    const elementId = uuid();
    const elementType = childrenToChooseFrom.find((et) => schemaTree.elements[et].name === name);
    if (!elementType) {
        console.warn("no element type found for " + el.name);
    }
    const result: XsdOperation[] = [{
        type: XsdOperationType.AddNode,
        createdOn: now,
        elementId,
        elementType,
        name,
        parentId,
        position: childPos,
    } as AddNodeXsdOperation];

    Object.keys(el.attributes || {}).forEach((k, idx) => {
        result.push({
            type: XsdOperationType.AddAttribute,
            attributeId: uuid(),
            createdOn: now,
            elementId,
            name: k,
            position: idx,
            value: el.attributes![k],
        } as AddAttributeXsdOperation);
    });
    if ('appinfo' === name || 'documentation' === name) {
        result.push({
            type: XsdOperationType.EditTextContent,
            elementId,
            newContent: js2xml( el, { spaces: 2 }),
        } as EditTextContentXsdOperation);

        return result;
    }

    const recChildren = elementType ? schemaTree.elements[elementType].children : [];

    (el.elements || []).forEach((el, idx) => {
        switch (el.type) {
            case 'element':
                const childDeltas = toDeltas(el, recChildren, elementId, idx);
                result.push(...childDeltas);
                break;
            case 'text':
                result.push({
                    type: XsdOperationType.EditTextContent,
                    elementId,
                    newContent: String(el.text),
                } as EditTextContentXsdOperation);
                break;
        }
    });

    return result;
}

/**
 * Validates and reads an already uploaded XSD file.
 *
 * @param fileId the ID of a previously uploaded file to read as XSD file.
 */
async function validateXsdFile(fileId: string) {
    const file = await FileModel.findById(fileId);
    if (!file) {
        throw new Error("Cannot find XSD file with ID " + fileId);
    }

    const contents = await read(file.getFullPath(), { encoding: 'utf8' });
    return xml2js(contents, { ignoreComment: true }) as Element;
}

export default function (app: Express, base: string) {
    app.post(`${base}/`, handlify(async (req, res) => {
        if (!ensureLoggedIn(req, res)) {
            return;
        }

        const userId = req.session!.userId;
        const name = req.body.name;
        const urlSlug = req.body.urlSlug;
        const xsdId = req.body.xsdId;

        if (slugify(urlSlug) !== urlSlug) {
            return res.status(400).json({
                success: false,
                msg: "URL slug not valid.",
            });
        }

        if (!await isProjectUrlSlugUnique(urlSlug)) {
            return res.status(400).json({
                success: false,
                msg: "URL slug is not unique.",
            });
        }

        let initialDeltas: XsdOperation[] | undefined;
        if (xsdId) {
            if (!ensureObjIdValid(res, xsdId)) {
                return;
            }

            let doc: Element;
            try {
                doc = await validateXsdFile(xsdId);
            } catch (err) {
                return res.status(500).json({
                    success: false,
                    msg: "Starter XSD file is invalid.",
                    details: err,
                });
            }

            if (!doc.elements) {
                return res.status(500).json({
                    success: false,
                    msg: "Start XSD file doesn't contain any elements.",
                });
            }

            initialDeltas = toDeltas(doc.elements[0], [ ElementTypes.XS_Schema ], null, 0);
        }

        await createProject(name, urlSlug, userId, initialDeltas);

        return res.status(200).json({
            success: true,
            msg: "Project created.",
        });
    }));

    app.post(`${base}/validate-xsd`, handlify(async (req, res) => {
        if (!ensureLoggedIn(req, res) || !ensurePresent(req, res, 'xsdId')) {
            return;
        }

        try {
            await validateXsdFile(req.body.xsdId);
        } catch (err) {
            return res.json({
                success: true,
                validationError: err,
            });
        }

        res.json({
            success: true,
            validationError: null,
        });
    }));
}
