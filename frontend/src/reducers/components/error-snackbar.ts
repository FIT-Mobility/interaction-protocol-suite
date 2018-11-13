import { Action, Types } from '../../actions';
import { ErrorSnackbarState } from "../../state";

export default function(
    state: ErrorSnackbarState = {isOpen: false, message: null},
    action: Action,
): ErrorSnackbarState {
    switch (action.type) {
        case Types.DisplayError:
            return {isOpen: true, message: action.payload};
        case Types.HideError:
            return {isOpen: false, message: ''};
        default:
            return state;
    }
}
