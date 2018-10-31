import { User } from 'omp-schema';
import { ThunkAction } from 'redux-thunk';

import { API_BACKEND_URL } from '../config';
import { State } from '../state';

import { Action, PayloadAction, Types } from '.';

export type UsersActions =
    | LoadUserAction;

export interface LoadUserAction extends PayloadAction<User> {
    type: Types.LoadUser;
}

export function fetchUser(id: string): ThunkAction<Promise<void>, State, void, Action> {
    return dispatch => fetch(`${API_BACKEND_URL}/user/${id}`)
        .then(resp => resp.json())
        .then(({ success, data, msg }) => {
            if (!success) {
                throw new Error(msg);
            }

            dispatch({
                type: Types.LoadUser,
                payload: data,
            } as LoadUserAction);
        })
        .catch(err => {
            console.error(`Failed fetching user ${id}:`);
            console.error(err);
        });
}
