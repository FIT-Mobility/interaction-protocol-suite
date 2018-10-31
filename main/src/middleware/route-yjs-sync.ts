/*
 * This redux middleware keeps the currently opened yjs project
 * in sync with the URL.
 */

import { Store } from 'redux';
import { replace as routerReplace, LOCATION_CHANGED } from 'redux-little-router';

import { Action } from '../actions';
import { closeProject, openProject } from '../actions/sync';
import { State } from '../state';

let oldProjectUrlSlug: string;
let oldProjectRevisionUrlSlug: string;

/**
 * Handles special cases for routing:
 * - In case an unknown route is requested (because the user tampered with the URL), redirect to '/'. Close potentially
 *   opened project revision rooms.
 * - In case some sub-route of /projects is requested, detect whether the URL slug of the project or the revision
 *   has changed. Dispatch openProject(new) and closeProject(old) as necessary.
 * - If the route does not match /projects/..., close any potentially opened project.
 * @param {Store<State, any>} store
 * @param action
 */
function handleLocationChange(store: Store<State, any>, action: any) {
    const isRouteKnown = action &&
        action.payload &&
        action.payload.route || false;

    if (!isRouteKnown || !action.payload.route.startsWith("/projects/")) {
        // close potentially opened project revision
        if (oldProjectUrlSlug) {
            store.dispatch(closeProject());
            oldProjectUrlSlug = "";
            oldProjectRevisionUrlSlug = "";
        }

        if (!isRouteKnown) {
            // redirect to project list, but use
            store.dispatch(routerReplace('/'));
        }
        return;
    }

    // from this point on, we know the matched route starts with /project
    const newProjectUrlSlug =
        action &&
        action.payload &&
        action.payload.params &&
        action.payload.params.projectUrlSlug ||
        undefined;

    const newProjectRevisionUrlSlug =
        action &&
        action.payload &&
        action.payload.params &&
        action.payload.params.revisionUrlSlug ||
        undefined;

    if (newProjectUrlSlug !== oldProjectUrlSlug) {
        // projectUrlSlug changed, is now possibly undefined
        if (newProjectUrlSlug && newProjectRevisionUrlSlug) {
            store.dispatch(openProject(newProjectUrlSlug, newProjectRevisionUrlSlug));
        } else if (oldProjectUrlSlug) {
            // either newProjectUrlSlug or newProjectRevisionUrlSlug was not set but currently a project is still open
            store.dispatch(closeProject());
            store.dispatch(routerReplace('/'));
        }
    } else if (newProjectRevisionUrlSlug !== oldProjectRevisionUrlSlug) {
        // project is still the same, but the project revision has changed
        if (newProjectUrlSlug && newProjectRevisionUrlSlug) {
            store.dispatch(openProject(newProjectUrlSlug, newProjectRevisionUrlSlug));
        } else if (oldProjectRevisionUrlSlug) {
            // either newProjectUrlSlug or newProjectRevisionUrlSlug was not set but currently a project is still open
            store.dispatch(closeProject());
            store.dispatch(routerReplace('/'));
        }
    }

    oldProjectUrlSlug = newProjectUrlSlug;
    oldProjectRevisionUrlSlug = newProjectRevisionUrlSlug;

    // Make sure that if one is not set, both are unset
    if (!oldProjectUrlSlug || !oldProjectRevisionUrlSlug) {
        oldProjectUrlSlug = "";
        oldProjectRevisionUrlSlug = "";
    }
}

export default (store: Store<State, any>) => (next: (ac: any) => any) => (action: Action) => {
    next(action);

    if (action.type === LOCATION_CHANGED) {
        handleLocationChange(store, action);
    }
};
