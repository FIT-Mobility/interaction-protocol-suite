import { User } from '@ips/shared-js';
import { push as routerPush } from 'redux-little-router';
import { ThunkAction } from 'redux-thunk';

import { Action, PayloadAction, Types } from '..';
import { API_BACKEND_URL, USER_LS_KEY } from '../../config';
import { LoginError, LoginErrorType, State } from '../../state';
import { init } from '../sync';

export type LoginActions =
    | ChangeLoginUserEmailAction
    | ChangeLoginUserNameAction
    | ChangeLoginUserPasswordAction
    | ChangeLoginUserPasswordRepeatAction
    | LoginStartAction
    | LoginFailAction
    | LoginFinishAction
    | SetLoginMessageAction;

export interface ChangeLoginUserEmailAction extends PayloadAction<string> {
    type: Types.ChangeLoginUserEmail;
}
export interface ChangeLoginUserPasswordAction extends PayloadAction<string> {
    type: Types.ChangeLoginUserPassword;
}
export interface ChangeLoginUserPasswordRepeatAction extends PayloadAction<string> {
    type: Types.ChangeLoginUserPasswordRepeat;
}
export interface ChangeLoginUserNameAction extends PayloadAction<string> {
    type: Types.ChangeLoginUserName;
}
export interface LoginStartAction {
    type: Types.Login_Start;
}
export interface LoginFailAction {
    type: Types.Login_Fail;
    error: true;
    payload: LoginError;
}
export interface LoginFinishAction extends PayloadAction<User> {
    type: Types.Login_Finish;
}
export interface SetLoginMessageAction extends PayloadAction<string | null> {
    type: Types.SetLoginMessage;
}

export function submit(): ThunkAction<Promise<void>, State, void, Action> {
    return async (dispatch, getState) => {
        const {userEmail, userPassword, userName} = getState().components.login;
        const routerResultState = getState().router.result;
        const isRegistering = routerResultState ? routerResultState.isRegistering : false;
        const postUrl = `${API_BACKEND_URL}/user${isRegistering ? '' : '/login'}`;
        const payload = isRegistering ? {
            email: userEmail,
            password: userPassword,
            name: userName,
        } : {
            email: userEmail,
            password: userPassword,
        };

        dispatch({type: Types.Login_Start} as LoginStartAction);
        let resp: Response;
        try {
            resp = await fetch(postUrl, {
                body: JSON.stringify(payload),
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                method: 'post',
            });
        } catch (err) {
            dispatch({
                type: Types.Login_Fail,
                error: true,
                payload: {
                    cause: LoginErrorType.Network,
                    message: err.message,
                },
            } as LoginFailAction);
            return;
        }

        const body = await resp.json();

        if (!resp.ok || !body.success) {
            dispatch({
                type: Types.Login_Fail,
                error: true,
                payload: {
                    cause: isRegistering ? LoginErrorType.Registration : LoginErrorType.Login,
                    message: body.msg,
                    status: resp.status,
                },
            } as LoginFailAction);
            return;
        }

        localStorage[USER_LS_KEY] = JSON.stringify(body.data);
        dispatch(emailInputChanged(''));
        dispatch(passwordInputChanged(''));
        dispatch(passwordRepeatInputChanged(''));
        dispatch(nameInputChanged(''));
        dispatch(setLoginMessage(null));
        dispatch({
            type: Types.Login_Finish,
            payload: body.data,
        } as LoginFinishAction);
        dispatch(init());
        if (isRegistering) {
            dispatch(routerPush('/'));
        }
    };
}

export function emailInputChanged(val: string): ChangeLoginUserEmailAction {
    return {
        type: Types.ChangeLoginUserEmail,
        payload: val,
    };
}

export function passwordInputChanged(pw: string): ChangeLoginUserPasswordAction {
    return {
        type: Types.ChangeLoginUserPassword,
        payload: pw,
    };
}

export function passwordRepeatInputChanged(pw: string): ChangeLoginUserPasswordRepeatAction {
    return {
        type: Types.ChangeLoginUserPasswordRepeat,
        payload: pw,
    };
}

export function nameInputChanged(name: string): ChangeLoginUserNameAction {
    return {
        type: Types.ChangeLoginUserName,
        payload: name,
    };
}

export function setLoginMessage(msg: string | null): SetLoginMessageAction {
    return {
        type: Types.SetLoginMessage,
        payload: msg,
    };
}
