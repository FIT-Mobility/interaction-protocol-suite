import { ThunkAction } from 'redux-thunk';

import { State } from '../../state';
import { Action, Types } from '../index';
import { NotifyLiveRevisionChangedAction } from '../sync';

export function discardNotification(): NotifyLiveRevisionChangedAction {
    return {
        type: Types.NotifyLiveRevisionChanged,
        payload: "",
    };
}

/**
 * ThunkAction that dispatches either a NotifyLiveRevisionChangedAction that sets the project URL slug for the
 * LiveRevisionChangeNotifier component, or that resets it. It emits the action that sets it in case the user is either
 * in the Editor component or the ProjectRevisionsList component and the corresponding project matches.
 * @param {string} projectUrlSlug URL slug of the project whose live revision changed
 * @returns {ThunkAction<Promise<void>, State, void, Action>}
 */
export function changeNotificationSlugIfOnSuitablePage(
    projectUrlSlug: string,
): ThunkAction<Promise<void>, State, void, Action> {
    return async (dispatch, getState) => {
        let isSuitablePage = false;
        const routerState = getState().router;
        if (routerState.params!.projectUrlSlug
            && routerState.pathname === `/projects/${routerState.params!.projectUrlSlug}/revisions`) {
            if (routerState.params!.projectUrlSlug === projectUrlSlug) {
                isSuitablePage = true;
            }
        } else if (routerState.params!.revisionUrlSlug) {
            // If revisionUrlSlug is known, projectUrlSlug must be known, too
            if (routerState.params!.projectUrlSlug === projectUrlSlug) {
                isSuitablePage = true;
            }
        }
        if (isSuitablePage) {
            dispatch({
                type: Types.NotifyLiveRevisionChanged,
                payload: projectUrlSlug,
            } as NotifyLiveRevisionChangedAction);
        } else {
            dispatch(discardNotification());
        }
    };
}
