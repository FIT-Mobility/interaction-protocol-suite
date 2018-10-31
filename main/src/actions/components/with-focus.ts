import { PayloadAction, Types } from "..";

export type FocusActions =
    | FocusComponentAction;

export interface FocusComponentAction extends PayloadAction<string | null> {
    type: Types.FocusComponent;
}

export function focusComponent(id: string | null): FocusComponentAction {
    return {
        type: Types.FocusComponent,
        payload: id,
    };
}
