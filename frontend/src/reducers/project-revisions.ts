import { ProjectRevision } from 'omp-schema';

import { Action, Types } from '../actions';

export default function(
    state: Record<string, ProjectRevision> = {},
    action: Action,
): Record<string, ProjectRevision> {
    switch (action.type) {
        case Types.AddProjectRevision:
        case Types.UpdateProjectRevision:
            return {
                ...state,
                [action.payload.id]: action.payload,
            };
        default:
            return state;
    }
}
