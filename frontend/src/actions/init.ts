import { ThunkAction } from 'redux-thunk';

import { USER_LS_KEY } from '../config';
import { State } from '../state';

import { Action, PayloadAction, Types } from '.';
import { setLoginMessage, LoginFinishAction } from './components/login';
import { connect, init } from './sync';

export type InitActions =
    | LogoutAction
    | NotifyIndexedDbAvailabilityKnownAction
    | NotifyLoginStatusKnownAction;

export interface LogoutAction {
    type: Types.Logout;
}

export interface NotifyIndexedDbAvailabilityKnownAction extends PayloadAction<boolean> {
    type: Types.NotifyIndexedDbAvailabilityKnown;
}
export interface NotifyLoginStatusKnownAction {
    type: Types.NotifyLoginStatusKnown;
}

export function assertIndexedDb(): ThunkAction<void, State, void, Action> {
    return async dispatch => {
        let idb;
        try {
            idb = await (new Promise((res, rej) => {
                const idbReq = indexedDB.open('OMPassert');
                idbReq.onsuccess = () => res(idbReq.result);
                idbReq.onerror = rej;
            }));
        } catch (err) {
            dispatch(notifyIndexedDbAvailabilityKnown(false));
            return;
        }

        idb.close();
        dispatch(notifyIndexedDbAvailabilityKnown(true));
    };
}

export function logout(): ThunkAction<void, State, void, Action> {
    return dispatch => {
        localStorage.removeItem(USER_LS_KEY);
        dispatch({ type: Types.Logout } as LogoutAction);
    };
}

export function notifyIndexedDbAvailabilityKnown(isAvailable: boolean): NotifyIndexedDbAvailabilityKnownAction {
    return {
        type: Types.NotifyIndexedDbAvailabilityKnown,
        payload: isAvailable,
    };
}

export function notifyLoginStatusKnown(): NotifyLoginStatusKnownAction {
    return { type: Types.NotifyLoginStatusKnown };
}

export function tryRelogin(): ThunkAction<Promise<void>, State, void, Action> {
    return async dispatch => {
        const lsData = localStorage[USER_LS_KEY];
        if (lsData) {
            try {
                await dispatch(connect());
                dispatch({ type: Types.Login_Finish, payload: JSON.parse(lsData) } as LoginFinishAction);
                dispatch(init());
            } catch (err) {
                // Connect throws an error if the login was rejected
                dispatch(setLoginMessage("Your session has timed out. Please login again."));
                dispatch(logout());
            }
        }

        dispatch(notifyLoginStatusKnown());
    };
}
