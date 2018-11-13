import { Action, Types } from '../../actions';
import { LoginComponentState } from '../../state';

export default function(
    state: LoginComponentState = {
        error: null,
        inProgress: false,
        message: null,
        userEmail: '',
        userPassword: '',
        userPasswordRepeat: '',
        isRegistering: false,
        userName: '',
    },
    action: Action,
): LoginComponentState {
    switch (action.type) {
        case Types.ChangeLoginUserEmail:
            return {
                ...state,
                error: null,
                userEmail: action.payload,
            };
        case Types.ChangeLoginUserPassword:
            return {
                ...state,
                error: null,
                userPassword: action.payload,
            };
        case Types.ChangeLoginUserPasswordRepeat:
            return {
                ...state,
                error: null,
                userPasswordRepeat: action.payload,
            };
        case Types.ChangeLoginUserName:
            return {
                ...state,
                error: null,
                userName: action.payload,
            };
        case Types.Login_Start:
            return {
                ...state,
                error: null,
                inProgress: true,
            };
        case Types.Login_Fail:
            return {
                ...state,
                error: action.payload,
                inProgress: false,
            };
        case Types.Login_Finish:
            return {
                ...state,
                error: null,
                inProgress: false,
            };
        case Types.SetLoginMessage:
            return {
                ...state,
                message: action.payload,
            };
        default:
            return state;
    }
}
