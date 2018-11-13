import slugify from "slugify";

import { Action, Types } from "../../actions";
import { ProjectRevisionsListComponentState } from "../../state";

export default function(
    state: ProjectRevisionsListComponentState = {
        archivationModalOpen: false,
        archivedProjectRevisionsListOpen: false,
        createSnapshotModalOpen: false,
        error: null,
        isCreateNewProjectRevisionInProgress: false,
        isRestoreRevisionInProgress: false,
        nameInput: '',
        projectRevisionToArchive: null,
        projectRevisionToClone: null,
        urlSlugInput: '',
    },
    action: Action,
): ProjectRevisionsListComponentState {
    switch (action.type) {
        case Types.ChangeNewProjectRevisionName:
            return {
                ...state,
                nameInput: action.payload,
                urlSlugInput: slugify(action.payload),
            };
        case Types.ChangeNewProjectRevisionUrlSlug:
            return {
                ...state,
                urlSlugInput: slugify(action.payload),
            };
        case Types.CloseProjectRevisionArchivationModal:
            return {
                ...state,
                archivationModalOpen: false,
            };
        case Types.CloseCreateSnapshotModal:
            return {
                ...state,
                createSnapshotModalOpen: false,
            };
        case Types.CreateNewProjectRevision_Start:
            return {
                ...state,
                error: null,
                isCreateNewProjectRevisionInProgress: true,
            };
        case Types.CreateNewProjectRevision_Fail:
            return {
                ...state,
                error: action.payload,
                isCreateNewProjectRevisionInProgress: false,
            };
        case Types.CreateNewProjectRevision_Finish:
            return {
                ...state,
                error: null,
                isCreateNewProjectRevisionInProgress: false,
            };
        case Types.RestoreRevision_Start:
            return {
                ...state,
                error: null,
                isRestoreRevisionInProgress: true,
            };
        case Types.RestoreRevision_Fail:
            return {
                ...state,
                error: action.payload,
                isRestoreRevisionInProgress: false,
            };
        case Types.RestoreRevision_Finish:
            return {
                ...state,
                error: null,
                isRestoreRevisionInProgress: false,
            };
        case Types.OpenProjectRevisionArchivationModal:
            return {
                ...state,
                projectRevisionToArchive: action.payload,
                archivationModalOpen: true,
            };
        case Types.OpenCreateSnapshotModal:
            return {
                ...state,
                projectRevisionToClone: action.payload,
                isCreateNewProjectRevisionInProgress: true,
                createSnapshotModalOpen: true,
            };
        case Types.ToggleProjectRevisionArchivationList:
            return {
                ...state,
                archivedProjectRevisionsListOpen: !state.archivedProjectRevisionsListOpen,
            };
        default:
            return state;
    }
}
