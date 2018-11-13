import { Dictionary } from 'lodash';
import {
    yjsShareGlobalProjects,
    yjsShareProjectRevision,
    ArrayDelta,
    Comment,
    DataTypeDocumentation,
    Entity,
    Function as Fn,
    OmpItem,
    Project,
    ProjectRevision,
    PROJECT_ROOM,
    Sequence,
    Service,
    XsdOperation,
} from 'omp-schema';
import { Dispatch } from 'redux';
import { replace as routerReplace } from 'redux-little-router';
import { ThunkAction, ThunkDispatch } from 'redux-thunk';
import * as socketIo from 'socket.io-client';
import { v4 as uuid } from 'uuid';
import yArray from 'y-array';
import yIndexedDb from 'y-indexeddb';
import yMap from 'y-map';
import yMemory from 'y-memory';
import yRichtext from 'y-richtext';
import yWebsocketsClient from 'y-websockets-client';
import Y from 'yjs';

import { Action, Types } from '..';
import { BACKEND_URL } from '../../config';
import { ConnectionState, State } from '../../state';
import { changeEditing } from '../components/change-editing';
import { changeNotificationSlugIfOnSuitablePage } from '../components/live-revision-change-notifier';

import {
    addXsdDeltas,
    ChangeConnectionStateAction,
    CloseProjectFailAction,
    CloseProjectFinishAction,
    CloseProjectStartAction,
    InitSyncFailAction,
    InitSyncFinishAction,
    InitSyncStartAction,
    OpenProjectFailAction,
    OpenProjectFinishAction,
    OpenProjectStartAction,
    RemoveXsdDeltasAction,
} from '.';

Y.extend(yMap, yArray, yRichtext, yWebsocketsClient, yMemory, yIndexedDb);

interface YArrayEvent<T> {
    type: 'insert' | 'delete';
    index: number;
    length: number;
    oldValues?: T[];
    values?: T[];
}

interface YMapEvent {
    type: 'add' | 'update' | 'delete';
    name: string;
    value?: any;
}

let projectToOpen = ["", ""]; // project ID, project revision ID
let ySocket;
/** The yjs room that contains all projects and project revisions. */
let yProjects;
/** The currently opened project revision */
let yProjectRevision;
/** The last known live revision IDs of all projects. Maps from project UUID to live revision UUID. */
const cachedLiveRevisions: Dictionary<string> = {};

export function connect(): ThunkAction<Promise<void>, State, void, Action> {
    return async dispatch => {
        if (ySocket) {
            return;
        }

        ySocket = socketIo(BACKEND_URL!);

        ySocket.on('connect', () => dispatch({
            type: Types.ChangeConnectionState,
            payload: ConnectionState.Connected,
        } as ChangeConnectionStateAction));
        ySocket.on('reconnecting', () => dispatch({
            type: Types.ChangeConnectionState,
            payload: ConnectionState.Disconnected,
        } as ChangeConnectionStateAction));

        const isAuthed = await (new Promise<boolean>(res => {
            ySocket.once('authorized', isAuthed => res(isAuthed));
        }));

        if (!isAuthed) {
            ySocket.close();
            ySocket = null;
            yProjects = null;

            throw new Error("Unauthorized");
        }
    };
}

/**
 * Returns a function that handles YProject updates that change the project's liveRevision field.
 *
 * @param {ThunkDispatch<State>} dispatch the dispatcher function
 * @returns {(event: YJSEvent) => any} the event handler function
 */
function createProjectLiveRevisionUpdateChangeHandler(
    dispatch: ThunkDispatch<State, void, Action>) {
    return (event: YMapEvent) => {
        // This callback is called whenever yProjects.share.projects.set() is called
        const projectFromEvent = event.value as Project;
        if (event.type === 'update') {
            const cachedLiveRevision = cachedLiveRevisions[projectFromEvent.id];
            if (!cachedLiveRevision) {
                throw new Error("Unable to find live revision!");
            }
            if (projectFromEvent.liveRevision !== cachedLiveRevision) {
                cachedLiveRevisions[projectFromEvent.id] = projectFromEvent.liveRevision;
                dispatch(changeNotificationSlugIfOnSuitablePage(projectFromEvent.urlSlug));
            }
        } else if (event.type === 'add') {
            cachedLiveRevisions[projectFromEvent.id] = projectFromEvent.liveRevision;
        } // Note: 'remove' is not handled because projects are never deleted (just archived)
    };
}

export function init(): ThunkAction<Promise<void>, State, void, Action> {
    return async dispatch => {
        dispatch({type: Types.InitSync_Start} as InitSyncStartAction);

        if (!ySocket) {
            try {
                await dispatch(connect());
            } catch (err) {
                dispatch({
                    type: Types.InitSync_Fail,
                    error: true,
                    payload: err,
                } as InitSyncFailAction);
            }
        }

        if (yProjects) {
            dispatch({type: Types.InitSync_Finish} as InitSyncFinishAction);
            return;
        }

        try {
            yProjects = await Y({
                db: {
                    name: 'indexeddb',
                },
                connector: {
                    name: 'websockets-client',
                    room: PROJECT_ROOM,
                    socket: ySocket,
                },
                share: yjsShareGlobalProjects,
            });
        } catch (error) {
            dispatch({
                type: Types.InitSync_Fail,
                payload: error,
                error: true,
            } as InitSyncFailAction);
        }

        intoRedux(yProjects.share.projects, {
            add: Types.AddProject,
            update: Types.UpdateProject,
            remove: Types.RemoveProject,
        }, dispatch);

        intoRedux(yProjects.share.projectRevisions, {
            add: Types.AddProjectRevision,
            update: Types.UpdateProjectRevision,
            remove: Types.RemoveProjectRevision,
        }, dispatch);

        // Observe the projects YMap to detect when the liveRevision of any project changes
        yProjects.share.projects.observe(createProjectLiveRevisionUpdateChangeHandler(dispatch));
        // Populate the cachedLiveRevisions array
        for (const projectId of yProjects.share.projects.keys()) {
            const project: Project = yProjects.share.projects.get(projectId);
            cachedLiveRevisions[projectId] = project.liveRevision;
        }

        dispatch({type: Types.InitSync_Finish} as InitSyncFinishAction);

        // Open possibly deferred project
        if (projectToOpen[0] !== "") {
            dispatch(openProject(projectToOpen[0], projectToOpen[1]));
            projectToOpen = ["", ""];
        }
    };
}

export function getYProjectRevision(): any {
    return yProjectRevision;
}

export function archiveProject(id: string): ThunkAction<void, State, void, Action> {
    return () => {
        const proj: Project = yProjects.share.projects.get(id);
        if (proj) {
            yProjects.share.projects.set(id, {
                ...proj,
                isArchived: true,
            } as Project);
        }
    };
}

export function archiveProjectRevision(id: string): ThunkAction<void, State, void, Action> {
    return () => {
        const projRev: ProjectRevision = yProjects.share.projectRevisions.get(id);
        if (projRev) {
            yProjects.share.projectRevisions.set(id, {
                ...projRev,
                isArchived: true,
            } as ProjectRevision);
        }
    };
}

export function closeProject(): ThunkAction<Promise<void>, State, void, Action> {
    return dispatch => {
        dispatch({type: Types.CloseProject_Start} as CloseProjectStartAction);

        return (yProjectRevision ? yProjectRevision.close() : Promise.resolve())
            .then(() => {
                yProjectRevision = null;
                dispatch({type: Types.CloseProject_Finish} as CloseProjectFinishAction);
                dispatch(changeEditing(true)); // allow editing of projects again
            })
            .catch(err => dispatch({
                type: Types.CloseProject_Fail,
                payload: err,
                error: true,
            } as CloseProjectFailAction));
    };
}

export function openProject(
    projectUrlSlug: string,
    revisionUrlSlug: string,
): ThunkAction<Promise<void>, State, void, Action> {
    return async dispatch => {
        // This action may be called in response to a router location change.
        // This can be problematic if this is called before init() has been run.
        //
        // A side effect of initialization is that the y socket is set. So if
        // this action is called before initialization has been done, defer
        // opening the project.
        if (!ySocket) {
            projectToOpen = [projectUrlSlug, revisionUrlSlug];
            return;
        }

        // FIXME Note: UI/Front-end does currently not handle OpenProject_Start or _Fail or _Finish!!
        dispatch({
            type: Types.OpenProject_Start,
            payload: projectUrlSlug,
        } as OpenProjectStartAction);

        // leave the current revision room, if we are in one
        if (yProjectRevision) {
            await yProjectRevision.close();
            yProjectRevision = null;
        }

        // check whether a project for the given projectUrlSlug actually exists
        let requestedProject: Project;
        for (const projectId of yProjects.share.projects.keys()) {
            const project: Project = yProjects.share.projects.get(projectId);
            if (project.urlSlug === projectUrlSlug) {
                requestedProject = project;
                break;
            }
        }

        if (!requestedProject!) {
            dispatch({
                type: Types.OpenProject_Fail,
                payload: new Error("No project found for the provided URL"),
                error: true,
            } as OpenProjectFailAction);
            dispatch(routerReplace('/'));
            return;
        }

        // next check whether the revision exists
        let revisionId = "";
        let revisionIsReadonly = false;
        if (revisionUrlSlug === "live") {
            revisionId = requestedProject!.liveRevision;
            const projectRevisionToOpen: ProjectRevision = yProjects.share.projectRevisions.get(revisionId);
            revisionIsReadonly = projectRevisionToOpen.readOnly;
        } else {
            for (const currentRevisionId of requestedProject!.revisions) {
                const currentRevision: ProjectRevision = yProjects.share.projectRevisions.get(currentRevisionId);
                if (currentRevision && currentRevision.urlSlug === revisionUrlSlug) {
                    revisionId = currentRevision.id;
                    revisionIsReadonly = currentRevision.readOnly;
                    break;
                }
            }
        }

        if (revisionId === "") {
            dispatch({
                type: Types.OpenProject_Fail,
                payload: new Error("No project revision found for the provided URL"),
                error: true,
            } as OpenProjectFailAction);
            dispatch(routerReplace('/'));
            return;
        }

        try {
            yProjectRevision = await Y({
                db: {
                    name: 'indexeddb',
                },
                connector: {
                    name: 'websockets-client',
                    room: revisionId,
                    socket: ySocket,
                },
                share: yjsShareProjectRevision,
            });
        } catch (err) {
            dispatch({
                type: Types.OpenProject_Fail,
                payload: err,
                error: true,
            } as OpenProjectFailAction);
            return;
        }

        /*
         * docTexts do not go into redux because they aren't serializable.
         * They are special-cased by the y-quill element.
         *
         * The same goes for the project docs.
         */

        intoRedux(yProjectRevision.share.comments, {
            add: Types.AddComment,
            update: Types.UpdateComment,
            remove: Types.RemoveComment,
        }, dispatch);
        intoRedux(yProjectRevision.share.dataTypeDocumentations, {
            add: Types.AddDataTypeDocumentation,
            update: Types.UpdateDataTypeDocumentation,
            remove: Types.RemoveDataTypeDocumentation,
        }, dispatch);
        intoRedux(yProjectRevision.share.functions, {
            add: Types.AddFunction,
            update: Types.UpdateFunction,
            remove: Types.RemoveFunction,
        }, dispatch);
        intoRedux(yProjectRevision.share.sequences, {
            add: Types.AddSequence,
            update: Types.UpdateSequence,
            remove: Types.RemoveSequence,
        }, dispatch);
        intoRedux(yProjectRevision.share.services, {
            add: Types.AddService,
            update: Types.UpdateService,
            remove: Types.RemoveService,
        }, dispatch);

        initXsdSync(dispatch);

        dispatch(changeEditing(revisionIsReadonly));

        dispatch({
            type: Types.OpenProject_Finish,
            payload: projectUrlSlug,
        } as OpenProjectFinishAction);
    };
}

export function unarchiveProject(id: string): ThunkAction<void, State, void, Action> {
    return () => {
        const proj: Project = yProjects.share.projects.get(id);
        if (proj) {
            yProjects.share.projects.set(id, {
                ...proj,
                isArchived: false,
            });
        }
    };
}

export function unarchiveProjectRevision(id: string): ThunkAction<void, State, void, Action> {
    return () => {
        const projRev: ProjectRevision = yProjects.share.projectRevisions.get(id);
        if (projRev) {
            yProjects.share.projectRevisions.set(id, {
                ...projRev,
                isArchived: false,
            });
        }
    };
}

export function updateRevision(id: string, update: Partial<ProjectRevision>): ThunkAction<void, State, void, Action> {
    return () => {
        if (!yProjects) {
            throw new Error("Missing y projects object");
        }

        const obj = yProjects.share.projectRevisions.get(id);
        yProjects.share.projectRevisions.set(id, {
            ...obj,
            ...update,
        });
    };
}

export function createComment(itemId: string, userId: string, text: string): ThunkAction<string, State, void, Action> {
    return assertProject(() => {
        const id = uuid();
        const comment: Comment = {
            createdBy: userId,
            createdOn: Date.now(),
            id,
            itemId,
            text,
        };

        yProjectRevision.share.comments.set(id, comment);
        return id;
    });
}

export function deleteComment(id: string): ThunkAction<void, State, void, Action> {
    return assertProject(() => yProjectRevision.share.comments.delete(id));
}

export function updateComment(id: string, text: string): ThunkAction<void, State, void, Action> {
    return updateItem<Comment>(yProjectRevision.share.comments, id, cmt => ({
        ...cmt,
        text,
    }));
}

export function createDataTypeDocumentationIfNotExists(id: string): ThunkAction<void, State, void, Action> {
    return assertProject(() => {
        if (!yProjectRevision.share.dataTypeDocumentations.get(id)) {
            yProjectRevision.share.dataTypeDocumentations.set(id, createDocumentation(id));
        }
    });
}

export function createFunction(name: string, reqDt: string, respDt: string): ThunkAction<string, State, void, Action> {
    return assertProject(() => {
        const [id, item] = createBase(name);
        const fn: Fn = {
            ...item as any,
            assertions: [],
            request: reqDt,
            response: respDt,
        };

        yProjectRevision.share.functions.set(id, fn);
        return id;
    });
}

export function deleteFunction(id: string): ThunkAction<void, State, void, Action> {
    return assertProject(() => yProjectRevision.share.functions.delete(id));
}

export function updateFunctionAssertions(id: string, assertions: string[]): ThunkAction<void, State, void, Action> {
    return updateItem<Fn>(yProjectRevision.share.functions, id, fn => ({
        ...fn,
        assertions,
    }));
}

export function updateFunctionName(id: string, name: string): ThunkAction<void, State, void, Action> {
    return updateItem<Fn>(yProjectRevision.share.functions, id, fn => ({
        ...fn,
        name,
    }));
}

export function updateFunctionRequestType(id: string, reqTypeId: string): ThunkAction<void, State, void, Action> {
    return updateItem<Fn>(yProjectRevision.share.functions, id, fn => ({
        ...fn,
        request: reqTypeId,
    }));
}

export function updateFunctionResponseType(id: string, respTypeId: string): ThunkAction<void, State, void, Action> {
    return updateItem<Fn>(yProjectRevision.share.functions, id, fn => ({
        ...fn,
        response: respTypeId,
    }));
}

export function createSequence(name: string, functions: string[]): ThunkAction<string, State, void, Action> {
    return assertProject(() => {
        const [id, item] = createBase(name);
        const seq: Sequence = {
            ...item as any,
            functions,
        };

        yProjectRevision.share.sequences.set(id, seq);
        return id;
    });
}

export function deleteSequence(id: string): ThunkAction<void, State, void, Action> {
    return assertProject(() => yProjectRevision.share.sequences.delete(id));
}

export function updateSequenceName(id: string, name: string): ThunkAction<void, State, void, Action> {
    return updateItem<Sequence>(yProjectRevision.share.sequences, id, seq => ({
        ...seq,
        name,
    }));
}

export function createService(
    name: string,
    functions: string[],
    sequences: string[],
): ThunkAction<string, State, void, Action> {
    return assertProject(() => {
        const [id, item] = createBase(name);
        const seq: Service = {
            ...item as any,
            functions,
            sequences,
        };

        yProjectRevision.share.services.set(id, seq);
        return id;
    });
}

export function deleteService(id: string): ThunkAction<void, State, void, Action> {
    return assertProject(() => yProjectRevision.share.services.delete(id));
}

export function toggleServiceFunction(id: string, fnId: string): ThunkAction<void, State, void, Action> {
    return updateItem<Service>(yProjectRevision.share.services, id, svc => ({
        ...svc,
        functions: svc.functions.indexOf(fnId) !== -1
            ? svc.functions.filter(sq => sq !== fnId)
            : [...svc.functions, fnId],
    }));
}

export function toggleServiceSequence(id: string, sqId: string): ThunkAction<void, State, void, Action> {
    return updateItem<Service>(yProjectRevision.share.services, id, svc => ({
        ...svc,
        sequences: svc.sequences.indexOf(sqId) !== -1
            ? svc.sequences.filter(sq => sq !== sqId)
            : [...svc.sequences, sqId],
    }));
}

export function updateServiceName(id: string, name: string): ThunkAction<void, State, void, Action> {
    return updateItem<Service>(yProjectRevision.share.services, id, svc => ({
        ...svc,
        name,
    }));
}

export function updateXsd(op: XsdOperation) {
    // Must .push works more like .concat in yjs
    return assertProject(() => yProjectRevision.share.xsd.push([op]));
}

/* Helper functions below */

/**
 * Ensures the y project is initialized before the callback is called
 * and throws an error otherwise.
 *
 * @param {(d: Dispatch<State>) => void} fn the callback to call when the y project is there
 * @returns {Thunk} the returned redux thunk
 */
function assertProject<T = void>(fn: (d: Dispatch<Action>) => T): ThunkAction<T, State, void, Action> {
    return dispatch => {
        if (!yProjectRevision) {
            throw new Error("Missing y project!");
        }

        return fn(dispatch);
    };
}

/**
 * Creates a basic OMP domain item with default properties.
 *
 * @param {string} name the name of the new item.
 * @returns {[string , OmpItem]} A tuple of the new ID and the item itself.
 */
function createBase(name: string): [string, OmpItem] {
    const id = uuid();
    return [id, {
        ...createDocumentation(id),
        createdOn: Date.now(),
        isArchived: false,
        name,
    }];
}
export const noItemBase: string = 'no-item-selected';

/**
 * Creates the basic structure of a data type object in yjs.
 *
 * @param {string} id the ID of the new data type.
 * @returns {DataTypeDocumentation} the structure.
 */
function createDocumentation(id: string): DataTypeDocumentation {
    return {
        // Typescript doesn't grok the [Language.German]: ... notation, so the keys
        // must be the literal enum values here, if we want proper typecking. :/
        docText: {
            en: createDocText(),
            de: createDocText(),
        },
        id,
    };
}

/**
 * Creates a new doc text entry and returns its ID.
 *
 * @returns {string} the ID of the newly created doc text.
 */
function createDocText(): string {
    if (!yProjectRevision) {
        throw new Error("Missing y project!");
    }

    const id = uuid();
    yProjectRevision.share.docTexts.set(id, Y.Richtext);
    return id;
}

/**
 * Set up y observers for the XSDs.
 *
 * @param dispatch the store's dispatch function.
 */
function initXsdSync(dispatch: Dispatch<Action>) {
    const yXsd = yProjectRevision.share.xsd;

    yXsd.observe((ev: YArrayEvent<XsdOperation>) => {
        if (ev.type === 'insert') {
            dispatch(addXsdDeltas(ev as ArrayDelta<XsdOperation>));
        } else {
            dispatch({
                type: Types.RemoveXsdDeltas,
                payload: {
                    index: ev.index,
                    length: ev.length,
                    values: ev.oldValues,
                },
            } as RemoveXsdDeltasAction);
        }
    });

    // Fill state with initial data
    const data = yXsd.toArray();
    dispatch(addXsdDeltas({
        index: 0,
        length: data.length,
        values: data,
    }));
}

/**
 * Sets up the y js observer and inserts the initial data into redux.
 *
 * @param {Object} yMap the Y map or y Array to observe
 * @param map the map from the Y js event type to the corresponding redux action
 * @param {Dispatch<State>} dispatch the dispatcher function
 */
function intoRedux(
    yMap: any,
    map: { add: Types, update: Types, remove: Types },
    dispatch: Dispatch<Action>,
) {
    /**
     * Returns a function that can be used to handle Y-JS data updates.
     *
     * @param map the map from the Y js event type to the corresponding redux action
     * @param {Dispatch<State>} dispatch the dispatcher function
     * @returns {(event: YJSEvent) => any} the event handler function
     */
    function createChangeHandler(
        map: { add: Types, update: Types, remove: Types },
        dispatch: Dispatch<Action>,
    ) {
        return (event: YMapEvent) => {
            if (event.type === 'add') {
                dispatch({type: map.add, payload: event.value} as Action);
            } else if (event.type === 'update') {
                dispatch({type: map.update, payload: event.value} as Action);
            } else {
                // removed
                // Note: 'name' is actually the items ID, event.value is not available!
                dispatch({type: map.remove, payload: event.name} as Action);
            }
        };
    }

    yMap.observe(createChangeHandler(map, dispatch));
    yMap.keys().forEach(k => dispatch({
        type: map.add,
        payload: yMap.get(k),
    } as Action));
}

/**
 * Updates the given y element using a function.
 *
 * @param yMap the yjs map to take the element from
 * @param {string} key the key of the element in the yjs map
 * @param {(item: T) => T} fn the updator function
 */
function updateItem<T extends Entity>(
    yMap: any,
    key: string, fn: (item: T) => T,
): ThunkAction<void, State, void, Action> {
    return assertProject(() => {
        const it = yMap.get(key);
        if (it) {
            yMap.set(key, fn(it));
        }
    });
}
