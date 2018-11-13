import { connect } from 'react-redux';

import OmpView, { OmpAppProps } from '../components/omp-app';
import { DEFAULT_TITLE } from '../config';
import { currentItemSelector, currentProjectRevisionSelector, currentProjectSelector } from '../selectors/project';
import { ConnectionState, State } from '../state';

const mapStateToProps = (state: State): OmpAppProps => {
    const currentProject = currentProjectSelector(state);
    const currentProjectRevision = currentProjectRevisionSelector(state);
    let barTitle = currentProject
        ? `Project: ${currentProject.name}`
        : DEFAULT_TITLE;

    const [it] = currentItemSelector(state);
    if (it) {
        barTitle = `${barTitle} > ${it.name}`;
    }

    let currentRevisionName = currentProject && currentProjectRevision ? currentProjectRevision.name : null;
    // Overwrite the currently opened revision name to "live" in case the opened revision is the live revision!
    if (currentRevisionName && currentProject!.liveRevision === currentProjectRevision!.id) {
        currentRevisionName = "live";
    }

    return {
        barTitle,
        currentUser: state.auth.users[state.auth.currentUser!],
        isIndexedDbAvailable: state.sync.indexedDbAvailable,
        isNetworkConnected: state.sync.connectionState === ConnectionState.Connected,
        loginStatusKnown: state.auth.statusKnown,
        pageTitle: it ? `OMP - ${it.name}` : DEFAULT_TITLE,
        currentRevisionName,
        currentRevisionsPageUrl: currentProject ? `/projects/${currentProject.urlSlug}/revisions` : '',
        isEditingDisabled: currentProjectRevision ? currentProjectRevision.readOnly : false,
        sync: state.sync,
    };
};
const mapDispatchToProps = {};

const OmpApp = connect(
    mapStateToProps,
    mapDispatchToProps,
)(OmpView);

export default OmpApp;
