/*
 * This redux middleware loads the user associated with a comment.
 */

import { Store } from 'redux';

import { Action, Types } from '../actions';
import { AddCommentAction } from '../actions/sync';
import { fetchUser } from '../actions/users';
import { State } from '../state';

function loadUser(store: Store<State, any>, action: AddCommentAction) {
    const userId = action.payload.createdBy;
    if (!(userId in store.getState().auth.users)) {
        store.dispatch(fetchUser(userId));
    }
}

export default (store: Store<State>) => (next: (ac: any) => any) => (action: Action) => {
    next(action);

    if (action.type === Types.AddComment) {
        loadUser(store, action);
    }
};
