import { PayloadAction, Types } from "..";

export type ChangeEditingActions =
    | ChangeEditingAction;

export interface ChangeEditingAction extends PayloadAction<boolean> {
    type: Types.ChangeEditing;
}

export function changeEditing(disable: boolean): ChangeEditingAction {
    return {
        type: Types.ChangeEditing,
        payload: disable,
    };
}
