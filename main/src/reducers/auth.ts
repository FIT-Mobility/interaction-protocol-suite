import { Action, Types } from '../actions';
import { AuthState } from '../state';

export default function(
    state: AuthState = {
        currentUser: null,
        statusKnown: false,
        users: {},
    },
    action: Action,
): AuthState {
    switch (action.type) {
        case Types.NotifyLoginStatusKnown:
            return {
                ...state,
                statusKnown: true,
            };
        case Types.LoadUser:
            return {
                ...state,
                users: {
                    ...state.users,
                    [action.payload.id]: action.payload,
                },
            };
        case Types.Login_Finish:
            return {
                ...state,
                currentUser: action.payload.id,
                statusKnown: true,
                users: {
                    ...state.users,
                    [action.payload.id]: action.payload,
                },
            };
        case Types.Logout:
            return {
                ...state,
                currentUser: null,
                statusKnown: true,
            };
        default:
            return state;
    }
}
