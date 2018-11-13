import { PayloadAction, Types } from "..";

export type ErrorSnackbarActions =
    | DisplayErrorAction
    | HideErrorAction;

export interface DisplayErrorAction extends PayloadAction<string | null> {
    type: Types.DisplayError;
}
export interface HideErrorAction {
    type: Types.HideError;
}

export function displayError(message: string): DisplayErrorAction {
    return {
        type: Types.DisplayError,
        payload: message,
    };
}
export function hideError(): HideErrorAction {
    return {
        type: Types.HideError,
    };
}
