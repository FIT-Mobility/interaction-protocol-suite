import slugify from 'slugify';

import { Action, Types } from '../../actions';
import { ProjectsListComponentState } from '../../state';

export default function(
    state: ProjectsListComponentState = {
        archivationModalOpen: false,
        archivedProjectListOpen: false,
        createProjectFromExistingSourcesError: null,
        createProjectFromExistingSourcesModalOpen: false,
        error: null,
        fileIdOfExistingXsd: null,
        isProcessingXsd: false,
        isCreateNewProjectInProgress: false,
        nameInput: '',
        needsAdditionalFiles: null,
        projectToArchive: null,
        urlSlugInput: '',
    },
    action: Action,
): ProjectsListComponentState {
    switch (action.type) {
        case Types.CloseCreateProjectFromExistingSourcesModal:
            return {
                ...state,
                createProjectFromExistingSourcesError: null,
                createProjectFromExistingSourcesModalOpen: false,
                fileIdOfExistingXsd: null,
                isProcessingXsd: false,
            };
        case Types.CloseProjectArchivationModal:
            return {
                ...state,
                archivationModalOpen: false,
                projectToArchive: null,
            };
        case Types.OpenCreateProjectFromExistingSourcesModal:
            return {
                ...state,
                createProjectFromExistingSourcesModalOpen: true,
            };
        case Types.OpenProjectArchivationModal:
            return {
                ...state,
                archivationModalOpen: true,
                projectToArchive: action.payload,
            };
        case Types.ProcessExistingXsd_Fail:
            return {
                ...state,
                createProjectFromExistingSourcesError: action.payload,
                isProcessingXsd: false,
            };
        case Types.ProcessExistingXsd_Finish:
            return {
                ...state,
                createProjectFromExistingSourcesError: null,
                fileIdOfExistingXsd: action.payload,
                isProcessingXsd: false,
            };
        case Types.ProcessExistingXsd_Start:
            return {
                ...state,
                createProjectFromExistingSourcesError: null,
                fileIdOfExistingXsd: null,
                isProcessingXsd: true,
            };
        case Types.ChangeNewProjectName:
            return {
                ...state,
                nameInput: action.payload,
                urlSlugInput: slugify(action.payload),
            };
        case Types.ChangeNewProjectUrlSlug:
            return {
                ...state,
                urlSlugInput: slugify(action.payload),
            };
        case Types.ToggleProjectArchivationList:
            return {
                ...state,
                archivedProjectListOpen: !state.archivedProjectListOpen,
            };
        case Types.CreateNewProject_Start:
            return {
                ...state,
                error: null,
                isCreateNewProjectInProgress: true,
            };
        case Types.CreateNewProject_Fail:
            return {
                ...state,
                error: action.payload,
                isCreateNewProjectInProgress: false,
            };
        case Types.CreateNewProject_Finish:
            return {
                ...state,
                error: null,
                isCreateNewProjectInProgress: false,
            };
        default:
            return state;
    }
}
