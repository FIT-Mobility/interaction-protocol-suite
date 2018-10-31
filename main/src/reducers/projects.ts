import { Project } from 'omp-schema';

import { Action, Types } from '../actions';

export default function(
    state: Record<string, Project> = {},
    action: Action,
): Record<string, Project> {
    switch (action.type) {
        case Types.AddProject:
        case Types.UpdateProject:
            return {
                ...state,
                [action.payload.id]: action.payload,
            };
        default:
            return state;
    }
}
