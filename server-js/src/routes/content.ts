import * as decompress from 'decompress';
import { Express } from 'express';
import * as fs from 'fs';
import * as path from 'path';
import { promisify } from 'util';
import { v4 as uuid } from 'uuid';

import { UPLOAD_PATH } from '../config';
import {
    createAcceptSingleFile,
    ensureLoggedIn,
    ensureObjIdValid,
    FileModel,
} from '../model';
import { getXsdString } from '../model/xsd';

import { handlify } from '.';

const acceptSingle = createAcceptSingleFile('file', UPLOAD_PATH);
const statP = promisify(fs.stat);

export default function(app: Express, base: string) {
    /**
     * Get a file by path inside a specific file group.
     */
    app.get(`${base}/group/:groupId/*`, handlify(async (req, res) => {
        const filePath = path.join(
            process.cwd(),
            UPLOAD_PATH,
            req.params.groupId,
            req.params[0],
        );

        console.log(`File ${req.params[0]} from ${req.params.groupId} requested. Returning ${filePath}.`);

        try {
            await statP(filePath);
        } catch (err) {
            if (err.code === 'ENOENT') {
                return res.status(404).json({
                    success: false,
                    msg: "File not found",
                });
            }

            console.error(err);
            return res.status(500).json({
                success: false,
                msg: "An unknown error occured.",
            });
        }

        res.download(filePath);
    }));

    /**
     * Declare a previously uploaded archive file a file group, which makes
     * the files inside the archive available below a certain URL path.
     */
    app.post(`${base}/group`, handlify(async (req, res) => {
        if (!ensureLoggedIn(req, res) || !ensureObjIdValid(res, req.body.fileId)) {
            return;
        }

        const zipFile = await FileModel.findById(req.body.fileId);
        if (!zipFile) {
            return res.status(404).json({
                success: false,
                msg: "File not found.",
            });
        }

        const groupId = uuid();

        let files: decompress.File[];
        try {
            files = await decompress(
                zipFile.getFullPath(),
                path.join(UPLOAD_PATH, groupId),
            );
        } catch (err) {
            return res.status(500).json({
                success: false, // tslint:disable-next-line:max-line-length
                msg: "Failed to extract file as archive. Could be a server error or because the file isn't an archive at all.",
            });
        }

        res.json({
            success: true,
            data: {
                files: files.filter(f => f.type === 'file')
                    .map(f => f.path),
                groupId,
            },
        });
    }));

    app.get(`${base}/xsd/:revisionId`, handlify(async (req, res) => {
        const projectRevisionId = req.params.revisionId;

        let xsd: string | null;
        try {
            xsd = await getXsdString(projectRevisionId);
        } catch (err) {
            return res.status(500).json({
                success: false,
                msg: err.message,
            });
        }
        if (!xsd) {
            return res.status(404).json({
                success: false,
                msg: `XSD for project revision ${projectRevisionId} not found.`,
            });
        }

        res.header({
            'Content-Disposition': `attachment; filename="schema-${projectRevisionId}.xsd"`,
            'Content-Type': 'text/xml',
        })
            .send(xsd);
    }));

    /**
     * Get a file from the server by ID.
     */
    // Must come below the group route to make the :fileId <-> group override work
    app.get(`${base}/:fileId`, handlify(async (req, res) => {
        if (!ensureObjIdValid(res, req.params.fileId)) {
            return;
        }

        const file = await FileModel.findById(req.params.fileId);
        if (!file) {
            return res.status(404).json({
                success: false,
                msg: "File not found.",
            });
        }

        // Don't set content-disposition headers here since this will also
        // be used for things like serving user avatars and such.
        res.contentType(file.mime)
            .sendFile(file.getFullPath());
    }));

    /**
     * Upload a file to the server.
     */
    app.post(base, handlify(async (req, res) => {
        if (!ensureLoggedIn(req, res)) {
            return;
        }

        try {
            await acceptSingle(req, res);
        } catch (err) {
            console.error(err);
            return res.status(400).json({
                success: false,
                msg: "Upload failed.",
            });
        }

        if (!req.file) {
            return res.status(400).json({
                success: false,
                msg: "Missing file or invalid file type.",
            });
        }

        const file = new FileModel({
            filename: req.file.filename,
            mime: req.file.mimetype,
        });
        try {
            await file.save();
        } catch (err) {
            console.log(err);
            return res.status(500).json({
                success: false,
                msg: "Failed to save file metadata to database.",
            });
        }

        res.json({
            success: true,
            data: file.safe(),
        });
    }));
}
