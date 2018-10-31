import { connect } from "react-redux";
import { createSelector } from "reselect";

import {
    archiveProjectRevision,
    closeArchivationModal,
    closeCreateSnapshotModal,
    closeProjectRevisionsOverview,
    createProjectRevision,
    nameInputChanged,
    openArchivationModal,
    openCreateSnapshotModal,
    openProjectRevision,
    restoreRevision,
    toggleArchivedProjectRevisionsList,
    unarchiveProjectRevision,
    urlSlugInputChanged
} from "../actions/components/project-revisions-list";
import ProjectRevisionsList,
{
    ProjectRevisionsListDispatch,
    ProjectRevisionsListProps
} from "../components/project-revisions-list";
import {
    currentProjectSelector,
    sortedProjectRevisionsSelector
} from "../selectors/project";
import { ConnectionState, State } from "../state";

const archivedProjectRevisionsSelector = createSelector(
    currentProjectSelector,
    sortedProjectRevisionsSelector,
    (currentProj, projRevs) => projRevs.filter(projRev => projRev.isArchived &&
        projRev.project === currentProj!.id).reverse(),
);
const notArchivedProjectRevisionsSelector = createSelector(
    currentProjectSelector,
    sortedProjectRevisionsSelector,
    (currentProj, projRevs) => projRevs.filter(projRev => !projRev.isArchived &&
        projRev.project === currentProj!.id),
);

const mapStateToProps = (state: State): ProjectRevisionsListProps => {
    const isNetworkConnected = state.sync.connectionState === ConnectionState.Connected;

    const currentProject = currentProjectSelector(state);
    const currentLiveRevision = currentProject!.liveRevision;

    const {
        archivationModalOpen,
        archivedProjectRevisionsListOpen,
        createSnapshotModalOpen,
        error,
        isCreateNewProjectRevisionInProgress,
        isRestoreRevisionInProgress,
        nameInput,
        projectRevisionToArchive,
        projectRevisionToClone,
        urlSlugInput,
    } = state.components.projectRevisionsList;

    const isInvalidRevisionURL = urlSlugInput === "live" || urlSlugInput === "revisions";

    return {
        archivationModalOpen,
        archivedProjectRevisions: archivedProjectRevisionsSelector(state),
        archivedProjectRevisionsOpen: archivedProjectRevisionsListOpen,
        canCreateProjectRevision: !!nameInput && !!urlSlugInput && isNetworkConnected && !isInvalidRevisionURL,
        createSnapshotModalOpen,
        currentLiveRevision,
        error,
        isCreatingProjectRevision: isCreateNewProjectRevisionInProgress,
        isRestoringRevision: isRestoreRevisionInProgress,
        isNetworkConnected,
        nameInput,
        projectRevisions: notArchivedProjectRevisionsSelector(state),
        projectRevisionToArchive: state.projectRevisions[projectRevisionToArchive!],
        projectRevisionToClone: state.projectRevisions[projectRevisionToClone!],
        urlSlugInput,
        isInvalidRevisionURL
    };
};

const mapDispatchToProps: ProjectRevisionsListDispatch = {
    archiveProjectRevision,
    closeArchivationModal,
    closeCreateSnapshotModal,
    closeProjectRevisionsOverview,
    createProjectRevision,
    nameInputChanged,
    openArchivationModal,
    openProjectRevision,
    openCreateSnapshotModal,
    restoreRevision,
    toggleArchivedProjectRevisionsList,
    unarchiveProjectRevision,
    urlSlugInputChanged,
};

const ProjectRevisions = connect(
    mapStateToProps,
    mapDispatchToProps
)(ProjectRevisionsList);

export default ProjectRevisions;
