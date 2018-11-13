import { connect } from 'react-redux';

import { pushInProject } from '../../actions';
import {
    changeXsdsFileToUpload,
    closeDownloadMenu,
    closeExistingXsdsMenu,
    closeProject,
    closeUploadMenu,
    downloadDocx,
    downloadPdf,
    openDownloadMenu,
    openExistingXsdsMenu,
    openUploadMenu,
    setDomainItemMenuCollapsation,
    setPreviewCollapsation,
    uploadExistingXsdsFile,
} from '../../actions/components/editor';
import { setAutoGeneratePreview } from '../../actions/components/editor/preview';
import EditorView, { EditorViewDispatch, EditorViewProps } from '../../components/editor';
import { API_BACKEND_URL } from '../../config';
import { currentProjectRevisionSelector, currentProjectSelector } from '../../selectors/project';
import { State } from '../../state';

const mapStateToProps = (state: State): EditorViewProps => ({
    autoGeneratePreview: state.components.preview.autoGenerate,
    currentProject: currentProjectSelector(state)!,
    existingXsdsFileUploadError: state.components.editor.existingXsdsFileUploadError,
    existingXsdsFileUploadInProgress: state.components.editor.existingXsdsFileUploadInProgress,
    hasExistingXsdFiles: Boolean(currentProjectRevisionSelector(state)!.importableFilesList.length),
    isDomainItemMenuCollapsed: state.components.editor.isDomainMenuCollapsed,
    isDownloadMenuOpen: state.components.editor.isDownloadMenuOpen,
    isExistingXsdsMenuOpen: state.components.editor.isExistingXsdsMenuOpen,
    isPreviewCollapsed: state.components.editor.isPreviewCollapsed,
    isUploadMenuOpen: state.components.editor.isUploadMenuOpen,
    disableEditing: state.components.disableEditing,
    downloadXsdLink: `${API_BACKEND_URL}/content/xsd/${currentProjectRevisionSelector(state)!.id}`,
});
const mapDispatchToProps: EditorViewDispatch = {
    closeDownloadMenu,
    closeExistingXsdsMenu,
    closeProject,
    closeUploadMenu,
    downloadDocx,
    downloadPdf,
    openDownloadMenu,
    openExistingXsdsMenu,
    openExistingXsdsViewer: () => pushInProject('/existing-files'),
    openUploadMenu,
    setAutoGeneratePreview,
    setDomainItemMenuCollapsation,
    setPreviewCollapsation,
    uploadExistingXsdsFile,
    uploadInputChanged: changeXsdsFileToUpload,
};

const Editor = connect(
    mapStateToProps,
    mapDispatchToProps,
)(EditorView);

export default Editor;
