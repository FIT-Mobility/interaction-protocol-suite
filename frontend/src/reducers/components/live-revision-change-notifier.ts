import { Action, Types } from '../../actions';
import { LiveRevisionChangeNotifierComponentState } from '../../state';

function createDefault(): LiveRevisionChangeNotifierComponentState {
    return {
        projectUrlSlugWithChangedLiveRevision: "",
    };
}

export default function (
    state: LiveRevisionChangeNotifierComponentState = createDefault(),
    action: Action,
): LiveRevisionChangeNotifierComponentState {
    switch (action.type) {
        case Types.NotifyLiveRevisionChanged:
            return {
                ...state,
                projectUrlSlugWithChangedLiveRevision: action.payload,
            };
        default:
            return state;
    }
}
