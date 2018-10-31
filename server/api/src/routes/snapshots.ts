import slugify from "slugify";

import { Express } from "express";

import { ensureLoggedIn } from "../model";
import {
    createScheduledBackupSnapshot,
    createSnapshot,
    isProjectRevisionUrlSlugUnique,
    restoreRevision
} from "../yjs";

import { handlify } from ".";

export default function (app: Express, base: string) {
    app.post(`${base}/`, handlify(async (req, res) => {
        console.log('Create snapshot');

        if (!ensureLoggedIn(req, res)) {
            return;
        }

        const userId = req.session!.userId;
        const projectRevisionUUID = req.body.projectRevisionUUID;
        const snapshotName = req.body.snapshotName;
        const snapshotSlug = req.body.snapshotSlug;

        if (slugify(snapshotSlug) !== snapshotSlug) {
            return res.status(400).json({
                success: false,
                msg: "URL slug not valid.",
            });
        }

        if (!userId || !projectRevisionUUID || !snapshotName || !snapshotSlug) {
            return res.status(400).json({
                success: false,
                msg: "Cannot create snapshot.",
            });
        }

        console.log('createSnapshot: creating snapshot');

        if (!await isProjectRevisionUrlSlugUnique(snapshotSlug, projectRevisionUUID)) {
            return res.status(400).json({
                success: false,
                msg: "URL slug is not unique.",
            });
        }

        await createSnapshot(userId, projectRevisionUUID, snapshotName, snapshotSlug);

        // return res.status(201).end();    // solution without response body
        return res.status(201).json({
            success: "true",
            msg: "Snapshot created.",
        });
    }));

    app.get(`${base}/archive`, handlify(async (req, res) => {
        console.log('Create scheduled backup snapshot');
        const backedupProjectsCount = await createScheduledBackupSnapshot();

        return res.status(backedupProjectsCount > 0 ? 201 : 200).json({
            success: "true",
            msg: backedupProjectsCount > 0 ? "Backup snapshots created." : "Nothing to back up.",
            count: backedupProjectsCount,
        });
    }));

    app.post(`${base}/restore`, handlify(async (req, res) => {
        console.log('Restore revision');
        if (!ensureLoggedIn(req, res)) {
            return;
        }

        const userId = req.session!.userId;
        const projectRevisionUUID = req.body.projectRevisionUUID;

        if (!projectRevisionUUID) {
            return res.status(404).json({
               success: false,
               msg: "Cannot restore a revision without UUID.",
            });
        }

        console.log('restoreRevision: restoring');

        await restoreRevision(projectRevisionUUID, userId);

        return res.status(200).json({
            success: "true",
            msg: "Revision restored.",
        });
    }));
}
