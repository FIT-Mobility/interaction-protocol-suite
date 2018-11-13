import { Types } from '../../actions';
import { ChangeEditingAction } from '../../actions/components/change-editing';

export default function(
    state: boolean = false,
    action: ChangeEditingAction,
): boolean {
    switch (action.type) {
        case Types.ChangeEditing:
            return action.payload;
        default:
            return state;
    }
}
