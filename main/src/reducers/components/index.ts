import { combineReducers } from 'redux';

import comments from './comments';
import disableEditing from './disable-editing';
import editor from './editor';
import errorSnackbar from './error-snackbar';
import existingFilesEditor from './existing-xsds-file-editor';
import focusedComponent from './focus-component';
import liveRevisionChangeNotifier from './live-revision-change-notifier';
import login from './login';
import preview from './preview';
import projectRevisionsList from './project-revisions-list';
import projectsList from './projects-list';
import xsdEditor from './xsd-editor';

export default combineReducers({
    comments,
    editor,
    errorSnackbar,
    existingFilesEditor,
    focusedComponent,
    login,
    preview,
    projectsList,
    projectRevisionsList,
    xsdEditor,
    liveRevisionChangeNotifier,
    disableEditing,
});
