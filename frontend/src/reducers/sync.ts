import { Action, Types } from '../actions';
import { ConnectionState, Sync } from '../state';

export default function(
    state: Sync = {
        connectionState: ConnectionState.Disconnected,
        error: null,
        indexedDbAvailable: true,
        initState: ConnectionState.Disconnected,
    },
    action: Action,
): Sync {
    switch (action.type) {
        case Types.InitSync_Start:
            return {
                ...state,
                initState: ConnectionState.Connecting,
            };
        case Types.InitSync_Fail:
            return {
                ...state,
                initState: ConnectionState.Disconnected,
                error: action.payload,
            };
        case Types.InitSync_Finish:
            return {
                ...state,
                initState: ConnectionState.Connected,
            };
        case Types.NotifyIndexedDbAvailabilityKnown:
            return {
                ...state,
                indexedDbAvailable: action.payload,
            };
        case Types.ChangeConnectionState:
            return {
                ...state,
                connectionState: action.payload,
            };
        default:
            return state;
    }
}
