import { connect } from 'react-redux';
import { createSelector } from 'reselect';

import {
    archiveProject,
    closeArchivationModal,
    closeCreateFromExistingSourcesModal,
    createProject,
    createProjectFromExistingSources,
    nameInputChanged,
    openArchivationModal,
    openCreateFromExistingSourcesModal,
    openProject,
    toggleArchivedProjectsList,
    unarchiveProject,
    urlSlugInputChanged,
    xsdFileInputChanged,
} from '../actions/components/projects-list';
import ProjectsList, { ProjectsListDispatch, ProjectsListProps } from '../components/projects-list';
import { sortedProjectsSelector } from '../selectors/project';
import { ConnectionState, State } from '../state';

const archivedProjectsSelector = createSelector(
    sortedProjectsSelector,
    projs => projs.filter(proj => proj.isArchived).reverse(),
);
const notArchivedProjectsSelector = createSelector(
    sortedProjectsSelector,
    projs => projs.filter(proj => !proj.isArchived),
);

const mapStateToProps = (state: State): ProjectsListProps => {
    const isNetworkConnected = state.sync.connectionState === ConnectionState.Connected;

    const {
        archivationModalOpen,
        archivedProjectListOpen,
        createProjectFromExistingSourcesModalOpen,
        createProjectFromExistingSourcesError,
        error,
        fileIdOfExistingXsd,
        isCreateNewProjectInProgress,
        isProcessingXsd,
        nameInput,
        projectToArchive,
        urlSlugInput,
    } = state.components.projectsList;

    return {
        archivedProjects: archivedProjectsSelector(state),
        archivedProjectsOpen: archivedProjectListOpen,
        canCreateProject: !!nameInput && !!urlSlugInput && isNetworkConnected,
        archivationModalOpen,
        canCreateProjectFromExistingSources: !!nameInput && !!fileIdOfExistingXsd,
        existingSourcesModalOpen: createProjectFromExistingSourcesModalOpen,
        isProcessingUploadedXsd: isProcessingXsd,
        nameInput,
        needsAdditionalFiles: null,
        urlSlugInput,
        projects: notArchivedProjectsSelector(state),
        projectToArchive: state.projects[projectToArchive!],
        error,
        isCreateNewProjectInProgress,
        xsdProcessingError: createProjectFromExistingSourcesError,
    };
};
const mapDispatchToProps: ProjectsListDispatch = {
    archiveProject,
    closeArchivationModal,
    closeCreateFromExistingSourcesModal,
    createProject,
    createProjectFromExistingSources,
    openCreateFromExistingSourcesModal,
    nameInputChanged,
    urlSlugInputChanged,
    openArchivationModal,
    openProject,
    xsdFileInputChanged,
    toggleArchivedProjectsList,
    unarchiveProject,
};

const Projects = connect(
    mapStateToProps,
    mapDispatchToProps,
)(ProjectsList);

export default Projects;
