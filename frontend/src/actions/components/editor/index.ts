import { Project, ProjectRevision } from "@ips/shared-js";
import { xsdToString } from '@ips/shared-js/xsd';
import { push as nativePush } from 'redux-little-router';
import { ThunkAction } from 'redux-thunk';

import { Action, PayloadAction, Types } from '../../';
import { API_BACKEND_URL } from '../../../config';
import { currentProjectRevisionSelector, currentProjectSelector } from '../../../selectors/project';
import { HttpError, State } from '../../../state';
import { updateRevision } from '../../sync';

import { CommentsActions } from './comments';
import { DomainMenuActions } from './domain-menu';
import { XsdEditorActions } from './editor-xsd';
import { Actions as ExistingFilesActions } from './existing-xsds-file-viewer';
import { PreviewActions } from './preview';
import { ValidationActions } from './validation';

export type EditorActions =
    | CommentsActions
    | DomainMenuActions
    | ExistingFilesActions
    | XsdEditorActions
    | PreviewActions
    | ValidationActions
    | SetDomainItemMenuCollapsationAction
    | SetPreviewCollapsationAction
    | OpenDownloadMenuAction
    | CloseDownloadMenuAction
    | OpenExistingXsdsMenuAction
    | CloseExistingXsdsMenuAction
    | OpenXsdsUploadMenuAction
    | CloseXsdsUploadMenuAction
    | ChangeXsdsFileToUploadAction
    | UploadExistingXsdsFileFailAction
    | UploadExistingXsdsFileStartAction
    | UploadExistingXsdsFileSuccessAction;

export interface CloseDownloadMenuAction {
    type: Types.CloseDownloadMenu;
}
export interface OpenDownloadMenuAction {
    type: Types.OpenDownloadMenu;
}
export interface ChangeXsdsFileToUploadAction extends PayloadAction<File> {
    type: Types.ChangeXsdsFileToUpload;
}
export interface CloseXsdsUploadMenuAction {
    type: Types.CloseXsdsUploadMenu;
}
export interface OpenXsdsUploadMenuAction {
    type: Types.OpenXsdsUploadMenu;
}
export interface CloseExistingXsdsMenuAction {
    type: Types.CloseExistingXsdsMenu;
}
export interface OpenExistingXsdsMenuAction {
    type: Types.OpenExistingXsdsMenu;
}
export interface SetDomainItemMenuCollapsationAction extends PayloadAction<boolean> {
    type: Types.SetDomainItemMenuCollapsation;
}
export interface SetPreviewCollapsationAction extends PayloadAction<boolean> {
    type: Types.SetPreviewCollapsation;
}
export interface UploadExistingXsdsFileFailAction extends PayloadAction<Error> {
    type: Types.UploadExistingXsdsFileFail;
    error: true;
}
export interface UploadExistingXsdsFileStartAction {
    type: Types.UploadExistingXsdsFileStart;
}
export interface UploadExistingXsdsFileSuccessAction {
    type: Types.UploadExistingXsdsFileSuccess;
}

const DIM_COLLAPSED_LS_KEY = 'OMP/DomainItemMenuCollapsed';
const PREVIEW_COLLAPSED_LS_KEY = 'OMP/PreviewCollapsed';

export function closeProject(): Action {
    return nativePush('/');
}

export function loadCollapsationStates(): ThunkAction<void, State, void, Action> {
    return dispatch => {
        dispatch(setDomainItemMenuCollapsation(localStorage[DIM_COLLAPSED_LS_KEY] === 'true'));
        dispatch(setPreviewCollapsation(localStorage[PREVIEW_COLLAPSED_LS_KEY] === 'true'));
    };
}

export function setDomainItemMenuCollapsation(collapse: boolean): ThunkAction<void, State, void, Action> {
    return (dispatch) => {
        localStorage[DIM_COLLAPSED_LS_KEY] = collapse;
        dispatch({
            type: Types.SetDomainItemMenuCollapsation,
            payload: collapse,
        } as SetDomainItemMenuCollapsationAction);
    };
}

export function setPreviewCollapsation(collapse: boolean): ThunkAction<void, State, void, Action> {
    return (dispatch) => {
        localStorage[PREVIEW_COLLAPSED_LS_KEY] = collapse;
        dispatch({
            type: Types.SetPreviewCollapsation,
            payload: collapse,
        } as SetPreviewCollapsationAction);
    };
}

export function downloadPdf(): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        dispatch(closeDownloadMenu());

        const s = getState();

        const [pdfUrl] = s.components.preview.previewLinks;
        if (!pdfUrl) {
            throw new Error((s.components.preview.previewLoadError as HttpError)!.message);
        }
        window.open(pdfUrl);
    };
}

export function downloadDocx(): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        dispatch(closeDownloadMenu());

        const s = getState();

        const [_, docxUrl] = s.components.preview.previewLinks;
        if (!docxUrl) {
            throw new Error((s.components.preview.previewLoadError as HttpError)!.message);
        }
        window.open(docxUrl);
    };
}

export function downloadXsd(): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        const state = getState();

        const proj: Project | null = currentProjectSelector(state);
        if (!proj) {
            throw new Error("Missing current project");
        }

        const projectRevision: ProjectRevision | null = currentProjectRevisionSelector(state);
        if (!projectRevision) {
            throw new Error("Missing current project revision");
        }

        dispatch(closeDownloadMenu());

        const { projectData } = state;
        triggerDownload(
            `${encodeURIComponent(proj.name)}.xsd`,
            xsdToString(
                projectData.xsd,
                projectData.xsdAnalysis ? projectData.xsdAnalysis.determineXsdPrefixIncludingColon() : '',
                projectRevision.baseUri
            ),
        );
    };
}

export function openDownloadMenu(): OpenDownloadMenuAction {
    return { type: Types.OpenDownloadMenu };
}

export function closeDownloadMenu(): CloseDownloadMenuAction {
    return { type: Types.CloseDownloadMenu };
}

export function openExistingXsdsMenu(): OpenExistingXsdsMenuAction {
    return { type: Types.OpenExistingXsdsMenu };
}

export function closeExistingXsdsMenu(): CloseExistingXsdsMenuAction {
    return { type: Types.CloseExistingXsdsMenu };
}

export function openUploadMenu(): OpenXsdsUploadMenuAction {
    return { type: Types.OpenXsdsUploadMenu };
}

export function closeUploadMenu(): CloseXsdsUploadMenuAction {
    return { type: Types.CloseXsdsUploadMenu };
}

export function changeXsdsFileToUpload(ev: React.ChangeEvent<HTMLInputElement>): ChangeXsdsFileToUploadAction {
    return {
        type: Types.ChangeXsdsFileToUpload,
        payload: ev.target.files![0],
    };
}

export function uploadExistingXsdsFile(): ThunkAction<Promise<void>, State, void, Action> {
    return async (dispatch, getState) => {
        dispatch(uplaodExistingXsdsFileStart());

        const state = getState();

        const file = state.components.editor.xsdsFileToUpload;
        if (!file) {
            throw new Error("Missing file to upload");
        }

        try {
            const fd = new FormData();
            fd.append("file", file, file.name);

            // First upload XSD file to server
            const resp = await fetch(`${API_BACKEND_URL}/content`, {
                body: fd,
                credentials: 'include',
                method: 'post',
            });

            const data = await resp.json();
            if (!resp.ok || !data.success) {
                dispatch(uploadExistingXsdsFileFail(new Error("Failed to upload XSD file to server.")));
                return;
            }

            const fileId = data.data.id;
            const resp2 = await fetch(`${API_BACKEND_URL}/content/group`, {
                body: JSON.stringify({ fileId }),
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' },
                method: 'post',
            });

            const data2 = await resp2.json();
            if (!resp2.ok || !data2.success) {
                dispatch(uploadExistingXsdsFileFail(new Error("Failed to unzip XSD file")));
                return;
            }

            const { files, groupId } = data2.data;
            const rev = currentProjectRevisionSelector(state);
            if (!rev || !groupId) {
                throw new Error("Missing project revision or group ID.");
            }

            dispatch(updateRevision(rev.id, {
                baseUri: `${API_BACKEND_URL}/content/group/${groupId}/`,
                importableFilesArchiveId: fileId,
                importableFilesList: files || [],
            }));
            dispatch(uplaodExistingXsdsFileSuccess());
        } catch (err) {
            dispatch(uploadExistingXsdsFileFail(err));
        }
    };
}

export function uplaodExistingXsdsFileStart(): UploadExistingXsdsFileStartAction {
    return { type: Types.UploadExistingXsdsFileStart };
}

export function uplaodExistingXsdsFileSuccess(): UploadExistingXsdsFileSuccessAction {
    return { type: Types.UploadExistingXsdsFileSuccess };
}

export function uploadExistingXsdsFileFail(err: Error): UploadExistingXsdsFileFailAction {
    return {
        type: Types.UploadExistingXsdsFileFail,
        error: true,
        payload: err,
    };
}

/**
 * Triggers the download of a text file.
 *
 * @param {string} filename the name of the file to be downloaded.
 * @param {string} textOrUrl the text contents or URL to be downloaded.
 * @param {string} type whether the textOrUrl parameter specifies text to be downloaded as text file or a URL.
 */
export function triggerDownload(filename: string, textOrUrl: string) {
    const element = document.createElement('a');
    element.setAttribute(
        'href',
        'data:text/plain;charset=utf-8,' + encodeURIComponent(textOrUrl),
    );
    element.setAttribute('download', filename);
    element.style.display = 'none';

    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
}
