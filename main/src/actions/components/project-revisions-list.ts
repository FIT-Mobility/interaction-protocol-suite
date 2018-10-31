import { push } from "redux-little-router";
import { ThunkAction } from "redux-thunk";

import { Action, Types } from "..";
import { API_BACKEND_URL } from "../../config";
import {
    CreateNewProjectRevisionError, CreateNewProjectRevisionErrorType, RestoreRevisionError, RestoreRevisionErrorType,
    State
} from "../../state";
import { pop, PayloadAction } from "../index";
import { archiveProjectRevision as acvProjRev, unarchiveProjectRevision } from "../sync";

export type ProjectRevisionsListActions =
    | ChangeNewProjectRevisionNameAction
    | ChangeNewProjectRevisionUrlSlugAction
    | CloseProjectRevisionArchivationModalAction
    | CloseCreateSnapshotModalAction
    | CreateNewProjectRevisionStartAction
    | CreateNewProjectRevisionFailAction
    | CreateNewProjectRevisionFinishAction
    | RestoreRevisionStartAction
    | RestoreRevisionFailAction
    | RestoreRevisionFinishAction
    | OpenProjectRevisionArchivationModalAction
    | OpenCreateSnapshotModalAction
    | ToggleProjectRevisionArchivationListAction;

export interface ChangeNewProjectRevisionNameAction extends PayloadAction<string> {
    type: Types.ChangeNewProjectRevisionName;
}

export interface ChangeNewProjectRevisionUrlSlugAction extends PayloadAction<string> {
    type: Types.ChangeNewProjectRevisionUrlSlug;
}

export interface CloseProjectRevisionArchivationModalAction {
    type: Types.CloseProjectRevisionArchivationModal;
}

export interface CloseCreateSnapshotModalAction {
    type: Types.CloseCreateSnapshotModal;
}

export interface CreateNewProjectRevisionStartAction {
    type: Types.CreateNewProjectRevision_Start;
}

export interface CreateNewProjectRevisionFailAction {
    type: Types.CreateNewProjectRevision_Fail;
    error: true;
    payload: CreateNewProjectRevisionError;
}

export interface CreateNewProjectRevisionFinishAction {
    type: Types.CreateNewProjectRevision_Finish;
}

export interface RestoreRevisionStartAction {
    type: Types.RestoreRevision_Start;
}

export interface RestoreRevisionFailAction {
    type: Types.RestoreRevision_Fail;
    error: true;
    payload: RestoreRevisionError;
}

export interface RestoreRevisionFinishAction {
    type: Types.RestoreRevision_Finish;
}

export interface OpenProjectRevisionArchivationModalAction extends PayloadAction<string> {
    type: Types.OpenProjectRevisionArchivationModal;
}

export interface OpenCreateSnapshotModalAction extends PayloadAction<string> {
    type: Types.OpenCreateSnapshotModal;
}

export interface ToggleProjectRevisionArchivationListAction {
    type: Types.ToggleProjectRevisionArchivationList;
}

export function closeArchivationModal(): CloseProjectRevisionArchivationModalAction {
    return {type: Types.CloseProjectRevisionArchivationModal};
}

export function closeCreateSnapshotModal(): CloseCreateSnapshotModalAction {
    return {type: Types.CloseCreateSnapshotModal};
}

export function openArchivationModal(id: string): OpenProjectRevisionArchivationModalAction {
    return {
        type: Types.OpenProjectRevisionArchivationModal,
        payload: id
    };
}

export function openCreateSnapshotModal(projRevId: string): OpenCreateSnapshotModalAction {
    return {
        type: Types.OpenCreateSnapshotModal,
        payload: projRevId
    };
}

export function archiveProjectRevision(id: string): ThunkAction<void, State, void, Action> {
    return dispatch => {
        dispatch(acvProjRev(id));
        dispatch(closeArchivationModal());
    };
}

export function openProjectRevision(urlSlug: string): ThunkAction<void, State, void, Action> {
    return dispatch => {
        dispatch(push(`${urlSlug}`));
    };
}

export function toggleArchivedProjectRevisionsList(): ToggleProjectRevisionArchivationListAction {
    return {type: Types.ToggleProjectRevisionArchivationList};
}

export function closeProjectRevisionsOverview(): ThunkAction<void, State, void, Action> {
    return dispatch => {
        dispatch(pop());
    };
}

export function nameInputChanged(name: string): ChangeNewProjectRevisionNameAction {
    return {
        type: Types.ChangeNewProjectRevisionName,
        payload: name,
    };
}

export function urlSlugInputChanged(urlSlug: string): ChangeNewProjectRevisionUrlSlugAction {
    return {
        type: Types.ChangeNewProjectRevisionUrlSlug,
        payload: urlSlug,
    };
}

export function createProjectRevision(
    projRev: string,
    name: string,
    urlSlug: string,
): ThunkAction<Promise<void>, State, void, Action> {
    return async (dispatch, getState) => {
        const payload = {
            projectRevisionUUID: projRev,
            snapshotName: name,
            snapshotSlug: urlSlug,
        };

        const postUrl = `${API_BACKEND_URL}/snapshots`;

        dispatch({ type: Types.CreateNewProjectRevision_Start } as CreateNewProjectRevisionStartAction);
        let resp: Response;
        try {
            resp = await fetch(postUrl, {
                body: JSON.stringify(payload),
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                method: 'post',
            });
        } catch (err) {
            dispatch({
                type: Types.CreateNewProjectRevision_Fail,
                error: true,
                payload: {
                    cause: CreateNewProjectRevisionErrorType.Network,
                    message: err.message,
                },
            } as CreateNewProjectRevisionFailAction);
            return;
        }

        const body = await resp.json();

        if (!resp.ok) {
            let errorType = CreateNewProjectRevisionErrorType.UrlSlugNotUnique;
            if (resp.status !== 400) {
                errorType = CreateNewProjectRevisionErrorType.UnexpectedServerError;
            }
            dispatch({
                type: Types.CreateNewProjectRevision_Fail,
                error: true,
                payload: {
                    cause: errorType,
                    message: body.msg,
                    status: resp.status,
                },
            } as CreateNewProjectRevisionFailAction);
            return;
        }

        dispatch(nameInputChanged(''));
        dispatch(urlSlugInputChanged(''));
        dispatch(closeCreateSnapshotModal());
        dispatch({type: Types.CreateNewProjectRevision_Finish} as CreateNewProjectRevisionFinishAction);
    };
}

export function restoreRevision(id: string): ThunkAction<Promise<void>, State, void, Action>  {
    return async (dispatch, getState) => {
        const payload = {
            projectRevisionUUID: id,
        };

        const postUrl = `${API_BACKEND_URL}/snapshots/restore`;

        dispatch({ type: Types.RestoreRevision_Start } as RestoreRevisionStartAction);
        let resp: Response;
        try {
            resp = await fetch(postUrl, {
                body: JSON.stringify(payload),
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                method: 'post',
            });
        } catch (err) {
            dispatch({
                type: Types.RestoreRevision_Fail,
                error: true,
                payload: {
                    cause: RestoreRevisionErrorType.Network,
                    message: err.message,
                },
            } as RestoreRevisionFailAction);
            return;
        }

        const body = await resp.json();

        if (!resp.ok) {
            dispatch({
                type: Types.RestoreRevision_Fail,
                error: true,
                payload: {
                    cause: RestoreRevisionErrorType.UnexpectedServerError,
                    message: body.msg,
                    status: resp.status,
                },
            } as RestoreRevisionFailAction);
            return;
        }

        dispatch({type: Types.RestoreRevision_Finish} as RestoreRevisionFinishAction);
    };
}

export { unarchiveProjectRevision };
