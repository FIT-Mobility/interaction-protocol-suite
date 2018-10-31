import { Action, Types } from '../../actions';

export default function(
    state: string | null = null,
    action: Action,
): string | null {
    switch (action.type) {
        case Types.FocusComponent:
            return action.payload;
        default:
            return state;
    }
}
