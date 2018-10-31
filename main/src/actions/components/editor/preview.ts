import { ThunkAction } from 'redux-thunk';

import { push, Action, PayloadAction, Types } from '../..';
import { API_BACKEND_URL, DOCX_MIME, JSON_MIME, PDF_MIME } from '../../../config';
import { generateReportData } from '../../../selectors/report-data';
import { ConnectionState, HttpError, PreviewMode, State } from '../../../state';

export type PreviewActions =
    | LoadPreviewFailAction
    | LoadPreviewFinishAction
    | LoadPreviewStartAction
    | SetAutoGeneratePreviewAction;

export interface LoadPreviewStartAction {
    type: Types.LoadPreview_Start;
}

export interface LoadPreviewFailAction extends PayloadAction<HttpError> {
    type: Types.LoadPreview_Fail;
    error: true;
}

export interface LoadPreviewFinishAction extends PayloadAction<[string, string]> {
    type: Types.LoadPreview_Finish;
}

export interface SetAutoGeneratePreviewAction extends PayloadAction<boolean> {
    type: Types.SetAutoGeneratePreview;
}

const AUTO_GEN_KEY = 'OMP/DontAutoGeneratePreview';

export function changePreviewMode(mode: PreviewMode): Action {
    return push({query: {preview: mode}});
}

export function generatePreview(): ThunkAction<Promise<void>, State, void, Action> {
    return async (dispatch, getState: () => State) => {
        dispatch({type: Types.LoadPreview_Start} as LoadPreviewStartAction);

        const state = getState();
        if (state.sync.connectionState !== ConnectionState.Connected) {
            dispatch({
                type: Types.LoadPreview_Fail,
                error: true,
                payload: {
                    message: "Not connected to the backend.",
                },
            } as LoadPreviewFailAction);
            return;
        }

        let resp: Response;
        try {
            resp = await fetch(`${API_BACKEND_URL}/report`, {
                body: JSON.stringify(generateReportData(state)),
                method: 'post',
                headers: {
                    'Accept': `${JSON_MIME}, ${PDF_MIME}, ${DOCX_MIME}`,
                    'Content-Type': JSON_MIME,
                },
            });
        } catch (err) {
            dispatch({
                type: Types.LoadPreview_Fail,
                error: true,
                payload: {
                    message: err.message,
                },
            } as LoadPreviewFailAction);
            return;
        }

        const body = await resp.json();
        if (!body.success || !resp.ok) {
            dispatch({
                type: Types.LoadPreview_Fail,
                error: true,
                payload: {
                    message: `Recieved invalid status code ${resp.status} from server: '${body.msg}'.`,
                    status: resp.status,
                },
            } as LoadPreviewFailAction);
            return;
        }

        dispatch({
            type: Types.LoadPreview_Finish,
            payload: [
                `${API_BACKEND_URL}/report/${body[PDF_MIME]}`,
                `${API_BACKEND_URL}/report/${body[DOCX_MIME]}`,
            ],
        } as LoadPreviewFinishAction);
    };
}

export function loadAutoGeneratePreview(): SetAutoGeneratePreviewAction {
    return setAutoGeneratePreview(localStorage[AUTO_GEN_KEY] !== 'true');
}

export function setAutoGeneratePreview(autoGen: boolean): SetAutoGeneratePreviewAction {
    localStorage[AUTO_GEN_KEY] = !autoGen;
    return {
        type: Types.SetAutoGeneratePreview,
        payload: autoGen,
    };
}
