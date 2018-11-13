import { ThunkAction } from 'redux-thunk';

import { push, Action, PayloadAction, Types } from '..';
import { API_BACKEND_URL } from '../../config';
import { CreateNewProjectError, CreateNewProjectErrorType, State } from '../../state';
import { archiveProject as acvProj, unarchiveProject } from '../sync';

export type ProjectsListActions =
    | ChangeNewProjectNameAction
    | CloseCreateProjectFromExistingSourcesModalAction
    | ChangeNewProjectUrlSlugAction
    | OpenProjectArchivationModalAction
    | CloseProjectArchivationModalAction
    | OpenCreateProjectFromExistingSourcesModalAction
    | CreateNewProjectStartAction
    | CreateNewProjectFailAction
    | CreateNewProjectFinishAction
    | ProcessExistingXsdFailAction
    | ProcessExistingXsdFinishAction
    | ProcessExistingXsdStartAction
    | RequireMissingXsdFilesAction
    | ToggleProjectArchivationListAction;

export interface ChangeNewProjectNameAction extends PayloadAction<string> {
    type: Types.ChangeNewProjectName;
}

export interface ChangeNewProjectUrlSlugAction extends PayloadAction<string> {
    type: Types.ChangeNewProjectUrlSlug;
}

export interface CreateNewProjectStartAction {
    type: Types.CreateNewProject_Start;
}

export interface CreateNewProjectFailAction {
    type: Types.CreateNewProject_Fail;
    error: true;
    payload: CreateNewProjectError;
}

export interface CreateNewProjectFinishAction {
    type: Types.CreateNewProject_Finish;
}

export interface CloseCreateProjectFromExistingSourcesModalAction {
    type: Types.CloseCreateProjectFromExistingSourcesModal;
}

export interface CloseProjectArchivationModalAction {
    type: Types.CloseProjectArchivationModal;
}

export interface OpenCreateProjectFromExistingSourcesModalAction {
    type: Types.OpenCreateProjectFromExistingSourcesModal;
}

export interface OpenProjectArchivationModalAction extends PayloadAction<string> {
    type: Types.OpenProjectArchivationModal;
}

export interface ProcessExistingXsdFailAction extends PayloadAction<Error | null> {
    error: true;
    type: Types.ProcessExistingXsd_Fail;
}

export interface ProcessExistingXsdFinishAction extends PayloadAction<string> {
    type: Types.ProcessExistingXsd_Finish;
}

export interface ProcessExistingXsdStartAction {
    type: Types.ProcessExistingXsd_Start;
}

export interface RequireMissingXsdFilesAction extends PayloadAction<string[]> {
    type: Types.RequireMissingXsdFiles;
}

export interface ToggleProjectArchivationListAction {
    type: Types.ToggleProjectArchivationList;
}

export function archiveProject(id: string): ThunkAction<void, State, void, Action> {
    return dispatch => {
        dispatch(acvProj(id));
        dispatch(closeArchivationModal());
    };
}

export function closeArchivationModal(): CloseProjectArchivationModalAction {
    return {type: Types.CloseProjectArchivationModal};
}

export function closeCreateFromExistingSourcesModal(): CloseCreateProjectFromExistingSourcesModalAction {
    return {type: Types.CloseCreateProjectFromExistingSourcesModal};
}

export function createProject(
    name: string,
    urlSlug: string,
    fileIdOfExistingXsd?: string,
): ThunkAction<Promise<void>, State, void, Action> {
    return async (dispatch, getState) => {
        const payload = {
            name,
            urlSlug,
            xsdId: fileIdOfExistingXsd,
        };
        const postUrl = `${API_BACKEND_URL}/project`;

        dispatch({ type: Types.CreateNewProject_Start } as CreateNewProjectStartAction);
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
                type: Types.CreateNewProject_Fail,
                error: true,
                payload: {
                    cause: CreateNewProjectErrorType.Network,
                    message: err.message,
                },
            } as CreateNewProjectFailAction);
            return;
        }

        const body = await resp.json();

        if (!resp.ok) {
            let errorType = CreateNewProjectErrorType.UrlSlugNotUnique;
            if (resp.status !== 400) {
                errorType = CreateNewProjectErrorType.UnexpectedServerError;
            }
            dispatch({
                type: Types.CreateNewProject_Fail,
                error: true,
                payload: {
                    cause: errorType,
                    message: body.msg,
                    status: resp.status,
                },
            } as CreateNewProjectFailAction);
            return;
        }

        // Clear inputs as the creation was successful
        dispatch(nameInputChanged(''));
        dispatch(urlSlugInputChanged(''));

        dispatch({type: Types.CreateNewProject_Finish} as CreateNewProjectFinishAction);
    };
}

export function createProjectFromExistingSources(): ThunkAction<void, State, void, Action> {
    return async (dispatch, getState) => {
        const { fileIdOfExistingXsd, nameInput, urlSlugInput } = getState().components.projectsList;

        if (!nameInput || !fileIdOfExistingXsd) {
            throw new Error("Missing project name or start XSD.");
        }

        await dispatch(createProject(nameInput, urlSlugInput, fileIdOfExistingXsd));
        dispatch(closeCreateFromExistingSourcesModal());
    };
}

export function nameInputChanged(name: string): ChangeNewProjectNameAction {
    return {
        type: Types.ChangeNewProjectName,
        payload: name,
    };
}

export function urlSlugInputChanged(urlSlug: string): ChangeNewProjectUrlSlugAction {
    return {
        type: Types.ChangeNewProjectUrlSlug,
        payload: urlSlug,
    };
}

export function openArchivationModal(id: string): OpenProjectArchivationModalAction {
    return {
        type: Types.OpenProjectArchivationModal,
        payload: id,
    };
}

export function openCreateFromExistingSourcesModal(): OpenCreateProjectFromExistingSourcesModalAction {
    return {type: Types.OpenCreateProjectFromExistingSourcesModal};
}

export function openProject(urlSlug: string): ThunkAction<void, State, void, Action> {
    return dispatch => {
        dispatch(nameInputChanged(''));
        dispatch(urlSlugInputChanged(''));
        dispatch(push(`/projects/${urlSlug}/live`));
    };
}

export function processXsdFail(err: Error): ProcessExistingXsdFailAction {
    return {
        type: Types.ProcessExistingXsd_Fail,
        error: true,
        payload: err,
    };
}

export function processXsdFinish(xsdFileId: string): ProcessExistingXsdFinishAction {
    return {
        type: Types.ProcessExistingXsd_Finish,
        payload: xsdFileId,
    };
}

export function processXsdStart(): ProcessExistingXsdStartAction {
    return {type: Types.ProcessExistingXsd_Start};
}

export function requireMissingXsdFiles(files: string[]): RequireMissingXsdFilesAction {
    return {
        type: Types.RequireMissingXsdFiles,
        payload: files,
    };
}

export function toggleArchivedProjectsList(): ToggleProjectArchivationListAction {
    return {type: Types.ToggleProjectArchivationList};
}

export function xsdFileInputChanged(el: HTMLInputElement): ThunkAction<void, State, void, Action> {
    return async (dispatch, getState) => {
        dispatch(processXsdStart());

        if (!el.files || !el.files.length) {
            dispatch(processXsdFail(new Error("Missing uploaded file.")));
            return;
        }

        let fileId: string;
        try {
            const fd = new FormData();
            fd.append("file", el.files[0], el.files[0].name);

            // First upload XSD file to server
            const resp = await fetch(`${API_BACKEND_URL}/content`, {
                body: fd,
                credentials: 'include',
                method: 'post',
            });
            const data = await resp.json();
            if (!resp.ok || !data.success) {
                dispatch(processXsdFail(new Error("Failed to upload XSD file to server.")));
                return;
            }

            // Then let it validate
            const validationResp = await fetch(`${API_BACKEND_URL}/project/validate-xsd`, {
                body: JSON.stringify({ xsdId: data.data.id }),
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                method: 'post',
            });
            const { success, validationError } = await validationResp.json();
            if (validationError) {
                dispatch(processXsdFail(validationError));
                return;
            } else if (!validationResp.ok || !success) {
                dispatch(processXsdFail(new Error("Failed to trigger XSD validation.")));
                return;
            }

            fileId = data.data.id;
        } catch (err) {
            console.error(err);
            dispatch(processXsdFail(err));
            return;
        }

        // Dispatch root element
        dispatch(processXsdFinish(fileId));
    };
}

export { unarchiveProject };
