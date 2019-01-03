import {
    yjsShareGlobalProjects,
    yjsShareProjectRevision,
    Project,
    ProjectRevision,
    PROJECT_ROOM,
    XsdOperation,
    XsdOperationType,
} from '@ips/shared-js';
import schema, { AttributeTypes, ElementTypes } from '@ips/shared-js/schema-tree';
import * as yLeveldb from '@ips/y-leveldb';
import * as dateformat from 'dateformat';
import { RequestHandler } from 'express';
import * as session from 'express-socket.io-session';
import { Server } from 'http';
import slugify from 'slugify';
import * as socketIo from 'socket.io';
import { v4 as uuid } from 'uuid';
import * as yArray from 'y-array';
import * as yMap from 'y-map';
import * as yRichtext from 'y-richtext';
import * as yWebsockets from 'y-websockets-server';
import * as Y from 'yjs';

Y.extend(yLeveldb, yWebsockets, yMap, yArray, yRichtext);

export const yInstances = {};
let io;

export function getY(room: string): Promise<any> {
    if (!yInstances[room]) {
        yInstances[room] = Y({
            db: {
                name: 'leveldb',
                dir: './db',
                namespace: room,
            },
            connector: {
                name: 'websockets-server',
                room,
                io,
                debug: true,
            },
            share: room === PROJECT_ROOM ? yjsShareGlobalProjects : yjsShareProjectRevision,
        });
    }
    return yInstances[room];
}

export default function (app: Server, sessionHandler: RequestHandler) {
    io = socketIo(app);
    io.use(session(sessionHandler));

    console.log('💫 Running y-websocket-server');

    io.on('connection', socket => {
        const rooms: string[] = [];
        const userId = (socket.handshake as any).session.userId; // Missing typings here

        if (!userId) {
            console.log("💥 Socket connection rejected");

            socket.emit('authorized', false);
            socket.disconnect();
            return;
        }

        console.log(`❤️ User ${userId} connected`);
        socket.emit('authorized', true);

        socket.on('joinRoom', async (room: string) => {
            const y = await getY(room);
            socket.join(room);
            if (rooms.indexOf(room) === -1) {
                y.connector.userJoined(socket.id, 'slave');
                rooms.push(room);
            }
        });

        socket.on('yjsEvent', async msg => {
            if (msg.room) {
                const y = await getY(msg.room);
                y.connector.receiveMessage(socket.id, msg);
            }
        });

        socket.on('disconnect', async () => {
            await Promise.all(rooms.map(async (room: string) => {
                const y = await getY(room);
                const i = rooms.indexOf(room);
                if (i >= 0) {
                    y.connector.userLeft(socket.id);
                    rooms.splice(i, 1);
                }
            }));

            console.log(`💔 User ${userId} disconnected`);
        });

        socket.on('leaveRoom', async (room: string) => {
            const y = await getY(room);
            const i = rooms.indexOf(room);
            if (i >= 0) {
                y.connector.userLeft(socket.id);
                rooms.splice(i, 1);
            }
        });
    });
}

export async function createProject(
    name: string,
    urlSlug: string,
    userId: string,
    initialDeltas: XsdOperation[] | undefined,
) {
    const yProjects = await getY(PROJECT_ROOM);
    const projectId = uuid();

    // Create project revision room first, before creating the corresponding project. This ensures that the room will
    // definitely exist once the user clicks on a project (the click is performed on the project that was added to the
    // global project room)
    const projectRevisionId = await createProjectRevision(
        userId,
        projectId,
        'live',
        'live',
        initialDeltas,
    );

    const project: Project = {
        createdOn: Date.now(),
        urlSlug,
        id: projectId,
        revisions: [projectRevisionId],
        liveRevision: projectRevisionId,
        isArchived: false,
        name,
    };
    yProjects.share.projects.set(projectId, project);
}

export async function isProjectUrlSlugUnique(slugToCheck: string): Promise<boolean> {
    const yProjects = (await getY(PROJECT_ROOM)).share.projects;
    return (yProjects.keys() as string[])
        .every(projectId => yProjects.get(projectId).urlSlug !== slugToCheck);
}

export async function isProjectRevisionUrlSlugUnique(
    slugToCheck: string,
    projectRevisionUUID: string
): Promise<boolean> {
    const yProjectRevisions = (await yInstances[PROJECT_ROOM]).share.projectRevisions;
    const projectId = yProjectRevisions.get(projectRevisionUUID).project;
    const projectRevisionsOfProject = (yProjectRevisions.keys() as string[])
        .filter(projRevId => yProjectRevisions.get(projRevId).project === projectId);
    return projectRevisionsOfProject
        .every(projRevId => yProjectRevisions.get(projRevId).urlSlug !== slugToCheck);
}

export async function createSnapshot(
    userId: string,
    projectRevisionToCloneId: string,
    snapshotName: string,
    snapshotSlug: string,
): Promise<string> {
    // create new projectRevision with new Id
    const yProjectsRoom = await getY(PROJECT_ROOM);
    const projectRevisionToClone: ProjectRevision = yProjectsRoom.share.projectRevisions.get(projectRevisionToCloneId);
    const projectId = projectRevisionToClone.project;

    const snapshotId = await createProjectRevision(userId, projectId, snapshotName, snapshotSlug,
        undefined, true);

    const [snapshotRoom, yProjectRevisionToCloneRoom] = await Promise.all([
        getY(snapshotId),
        getY(projectRevisionToCloneId),
    ]);

    // copy every element
    await copyRevisionElement(
        yProjectRevisionToCloneRoom.share.comments,
        snapshotRoom.share.comments,
        projectRevisionToCloneId,
        snapshotId
    );

    await copyRevisionElement(
        yProjectRevisionToCloneRoom.share.dataTypeDocumentations,
        snapshotRoom.share.dataTypeDocumentations,
        projectRevisionToCloneId,
        snapshotId
    );

    await copyRevisionElement(
        yProjectRevisionToCloneRoom.share.functions,
        snapshotRoom.share.functions,
        projectRevisionToCloneId,
        snapshotId
    );

    await copyRevisionElement(
        yProjectRevisionToCloneRoom.share.sequences,
        snapshotRoom.share.sequences,
        projectRevisionToCloneId,
        snapshotId
    );

    await copyRevisionElement(
        yProjectRevisionToCloneRoom.share.services,
        snapshotRoom.share.services,
        projectRevisionToCloneId,
        snapshotId
    );

    snapshotRoom.share.xsd.push(yProjectRevisionToCloneRoom.share.xsd.toArray());

    // Append new snapshot's ID to the project revisions list
    const yProject = yProjectsRoom.share.projects.get(projectId);
    yProject.revisions.push(snapshotId);
    yProjectsRoom.share.projects.set(projectId, yProject);

    // Copy project docText, because createProjectRevision() always creates new references for empty YRichtexts
    copyDocText(projectRevisionToClone.docText, projectRevisionToCloneId, snapshotId);
    const snapshot: ProjectRevision = yProjectsRoom.share.projectRevisions.get(snapshotId);
    snapshot.docText = projectRevisionToClone.docText;
    yProjectsRoom.share.projectRevisions.set(snapshotId, snapshot);

    return snapshotId;
}

export async function createScheduledBackupSnapshot(): Promise<number> {
    const yProjectsRoom = await yInstances[PROJECT_ROOM];
    if (!yProjectsRoom) {
        // Not even the project room was entered since last server restart - so there is certainly nothing to back up!
        console.log("No projects needed to be backed up (project room not entered since last backup).");
        return 0;
    }

    let backedUpProjects = 0;
    for (const projectId of yProjectsRoom.share.projects.keys()) {
        const project: Project = yProjectsRoom.share.projects.get(projectId);

        // Verify that the server actually has the corresponding live project revision room opened (this would only be
        // the case if at least one user opened the live revision since the last server start)
        if (!(project.liveRevision in yInstances)) {
            continue;
        }

        const dateAsString = dateformat(new Date(), "yyyy-mm-dd HH:MM");
        await createSnapshot(
            "-1",
            project.liveRevision,
            `Auto-backup ${dateAsString}`,
            // URL may not contain : otherwise the router does not recognize the route
            slugify(dateAsString.replace(':', ''))
        );
        console.log(`Backed up live revision of project with URL-slug ${project.urlSlug}`);
        backedUpProjects += 1;
    }

    if (0 === backedUpProjects) {
        console.log("No projects needed to be backed up (no live rooms entered since last backup).");
    }

    return backedUpProjects;
}

export async function restoreRevision(
    revisionToRestore: string,
    userId: string,
) {
    const yProjectsRoom = await yInstances[PROJECT_ROOM];
    const projectRevisionToRestore: ProjectRevision = yProjectsRoom.share.projectRevisions.get(revisionToRestore);
    const projectId = projectRevisionToRestore.project;
    const project: Project = yProjectsRoom.share.projects.get(projectId);
    const currentLiveRevisionId = project.liveRevision;
    const currentLiveRevision: ProjectRevision = yProjectsRoom.share.projectRevisions.get(currentLiveRevisionId);

    currentLiveRevision.name = (new Date()).toLocaleString();
    currentLiveRevision.urlSlug = slugify(currentLiveRevision.name.replace(/:/g, '-'));
    yProjectsRoom.share.projectRevisions.set(currentLiveRevisionId, currentLiveRevision);

    let newProjectRevisionId;
    if (projectRevisionToRestore.readOnly) {
        // use the createSnapshot() method (which creates a copy of the revisionToRestore room) as a "trick" to create
        // a new live ProjectRevision room
        newProjectRevisionId = await createSnapshot(
            userId,
            revisionToRestore,
            'live',
            'live'
        );
        // The only thing left to fix is the readOnly attribute
        const newProjectRevision: ProjectRevision = yProjectsRoom.share.projectRevisions.get(newProjectRevisionId);
        newProjectRevision.readOnly = false;
        yProjectsRoom.share.projectRevisions.set(newProjectRevisionId, newProjectRevision);
    } else {
        // No need to copy any rooms, because projectRevisionToRestore is a former live revision
        projectRevisionToRestore.name = "live";
        yProjectsRoom.share.projectRevisions.set(revisionToRestore, projectRevisionToRestore);
        newProjectRevisionId = revisionToRestore;
    }

    // Set the new live revision in the corresponding Project object
    project.liveRevision = newProjectRevisionId;
    yProjectsRoom.share.projects.set(projectId, project);
}

async function copyRevisionElement(
    sourceYmap: any,
    destYmap: any,
    projectRevisionToCloneId: string,
    snapshotId: string,
) {
    for (const key of sourceYmap.keys()) {
        const copyObject = {};
        const value = sourceYmap.get(key);
        for (const vkey of Object.keys(value)) {
            if (vkey === "docText") {
                await copyDocText(value[vkey], projectRevisionToCloneId, snapshotId);
            }
            copyObject[vkey] = value[vkey];
        }
        destYmap.set(key, copyObject);
    }
}

async function copyDocText(
    docText: any,
    projectRevisionToCloneId: string,
    snapshotId: string,
): Promise<void> {
    const englishUuid = docText.en;
    const germanUuid = docText.de;

    const snapshotRoom = await getY(snapshotId);
    const englishRichtext = snapshotRoom.share.docTexts.set(englishUuid, Y.Richtext);
    const germanRichtext = snapshotRoom.share.docTexts.set(germanUuid, Y.Richtext);

    const projectRevisionToCloneRoom = await getY(projectRevisionToCloneId);
    const englishDelta = projectRevisionToCloneRoom.share.docTexts.get(englishUuid).toDelta();
    const germanDelta = projectRevisionToCloneRoom.share.docTexts.get(germanUuid).toDelta();

    englishRichtext.applyDelta({ops: englishDelta});
    germanRichtext.applyDelta({ops: germanDelta});
}

async function createProjectRevision(
    userId: string,
    projectId: string,
    name: string,
    urlSlug: string,
    initialDeltas: XsdOperation[] = createInitialDeltas(),
    isSnapshot: boolean = false,
): Promise<string> {
    const projectRevisionId: string = uuid();
    const yProjectsRoom = await getY(PROJECT_ROOM);
    const yProjectRevisionRoom = await getY(projectRevisionId);
    const englishUuid = uuid();
    const germanUuid = uuid();
    const projectRevision: ProjectRevision = {
        baseUri: null,
        createdOn: Date.now(),
        docText: {
            en: englishUuid,
            de: germanUuid,
        },
        imports: [],
        importableFilesArchiveId: null,
        importableFilesList: [],
        urlSlug,
        id: projectRevisionId,
        isArchived: false,
        name,
        creatorId: userId,
        readOnly: isSnapshot,
        project: projectId,
    };

    yProjectsRoom.share.projectRevisions.set(projectRevisionId, projectRevision);

    yProjectRevisionRoom.share.docTexts.set(englishUuid, Y.Richtext);
    yProjectRevisionRoom.share.docTexts.set(germanUuid, Y.Richtext);

    // Initially add a XSD root element in case of a new project
    if (!isSnapshot) {
        yProjectRevisionRoom.share.xsd.push(initialDeltas);
    }

    return projectRevisionId;
}

function createInitialDeltas() {
    const rootElId = uuid();
    const now = Date.now();
    return [{
        type: XsdOperationType.AddNode,
        createdOn: now,
        elementId: rootElId,
        elementType: ElementTypes.XS_Schema,
        name: schema.elements[ElementTypes.XS_Schema].name,
        parentId: null,
        position: 0,
    }, {
        type: XsdOperationType.AddAttribute,
        attributeId: uuid(),
        createdOn: now,
        elementId: rootElId,
        name: 'xmlns',
        position: 0,
        value: 'http://www.w3.org/2001/XMLSchema',
    }, {
        type: XsdOperationType.AddAttribute,
        attributeId: uuid(),
        createdOn: now,
        elementId: rootElId,
        name: schema.attributes[AttributeTypes.TargetNameSpace].name,
        position: 1,
        value: 'http://www.dimo.de',
    }, {
        type: XsdOperationType.AddAttribute,
        attributeId: uuid(),
        createdOn: now,
        elementId: rootElId,
        name: 'xmlns:tns',
        position: 2,
        value: 'http://www.dimo.de',
    }] as XsdOperation[];
}
