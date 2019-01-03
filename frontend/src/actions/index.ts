import { IdAble } from '@ips/shared-js';
import { goBack, push as routerPush, RouterActions } from 'redux-little-router';

import { ChangeEditingActions } from './components/change-editing';
import { EditorActions } from './components/editor';
import { ErrorSnackbarActions } from "./components/error-snackbar";
import { LoginActions } from './components/login';
import { ProjectRevisionsListActions } from './components/project-revisions-list';
import { ProjectsListActions } from './components/projects-list';
import { FocusActions } from "./components/with-focus";
import { InitActions } from './init';
import { SyncActions } from './sync';
import { UsersActions } from './users';

export type Action =
    | EditorActions
    | FocusActions
    | ChangeEditingActions
    | ErrorSnackbarActions
    | InitActions
    | LoginActions
    | ProjectsListActions
    | ProjectRevisionsListActions
    | SyncActions
    | UsersActions
    | RouterActions
    | __NonExhaustiveMatch;

export interface PayloadAction<T> {
    payload: T;
}

export type RemoveAction = PayloadAction<string>;
export type Update<T extends IdAble> = Partial<T> & IdAble;

/* tslint:disable-next-line:class-name */
export interface __NonExhaustiveMatch {
    type: Types.__NonExhaustive;
}

export const enum Types {
    AddComment = 'ADD_COMMENT',
    RemoveComment = 'REMOVE_COMMENT',
    UpdateComment = 'UPDATE_COMMENT',

    AddDataType = 'ADD_DATATYPE',
    RemoveDataType = 'REMOVE_DATATYPE',
    UpdateDataType = 'UPDATE_DATATYPE',
    AddDataTypeDocumentation = 'ADD_DATATYPE_DOCUMENTATION',
    RemoveDataTypeDocumentation = 'REMOVE_DATATYPE_DOCUMENTATION',
    UpdateDataTypeDocumentation = 'UPDATE_DATATYPE_DOCUMENTATION',

    AddElement = 'ADD_ELEMENT',
    RemoveElement = 'REMOVE_ELEMENT',
    UpdateElement = 'UPDATE_ELEMENT',

    AddFunction = 'ADD_FUNCTION',
    RemoveFunction = 'REMOVE_FUNCTION',
    UpdateFunction = 'UPDATE_FUNCTION',

    AddSequence = 'ADD_SEQUENCE',
    RemoveSequence = 'REMOVE_SEQUENCE',
    UpdateSequence = 'UPDATE_SEQUENCE',

    AddService = 'ADD_SERVICE',
    RemoveService = 'REMOVE_SERVICE',
    UpdateService = 'UPDATE_SERVICE',

    AddXsdDeltas = 'ADD_XSD_DELTAS',
    RemoveXsdDeltas = 'REMOVE_XSD_DELTAS',
    FocusXsdElement = 'FOCUS_XSD_ELEMENT',

    FocusComponent = 'FOCUS_COMPONENT',

    ChangeEditing = 'CHANGE_EDITING',

    AddProject = 'ADD_PROJECT',
    RemoveProject = 'REMOVE_PROJECT',
    UpdateProject = 'UPDATE_PROJECT',

    AddProjectRevision = 'ADD_PROJECT_REVISION',
    RemoveProjectRevision = 'REMOVE_PROJECT_REVISION',
    UpdateProjectRevision = 'UPDATE_PROJECT_REVISION',

    UpdateSchemaAnalysis = 'UPDATE_SCHEMA_ANALYSIS',

    OpenProject_Start = 'OPEN_PROJECT_START',
    OpenProject_Fail = 'OPEN_PROJECT_FAIL',
    OpenProject_Finish = 'OPEN_PROJECT_FINISH',

    CloseProject_Start = 'CLOSE_PROJECT_START',
    CloseProject_Fail = 'CLOSE_PROJECT_FAIL',
    CloseProject_Finish = 'CLOSE_PROJECT_FINISH',

    DisplayError = 'DISPLAY_ERROR',
    HideError = 'HIDE_ERROR',

    LoadPreview_Start = 'LOAD_PREVIEW_START',
    LoadPreview_Fail = 'LOAD_PREVIEW_FAIL',
    LoadPreview_Finish = 'LOAD_PREVIEW_FINISH',

    LoadUser = 'LOAD_USER',

    ChangeCommentText = 'CHANGE_COMMENT_TEXT',
    ClickCommentReference = 'click-comment-ref', // This is a DOM event name -> kebap-case
    OpenCommentDeletionModal = 'OPEN_COMMENT_DELETION_MODAL',
    CloseCommentDeletionModal = 'CLOSE_COMMENT_DELETION_MODAL',

    ChangeLoginUserEmail = 'CHANGE_LOGIN_USER_EMAIL',
    ChangeLoginUserPassword = 'CHANGE_LOGIN_USER_PASSWORD',
    ChangeLoginUserPasswordRepeat = 'CHANGE_LOGIN_USER_PASSWORD_REPEAT',
    ChangeLoginUserName = 'CHANGE_LOGIN_USER_NAME',
    SetLoginMessage = 'SET_LOGIN_MESSAGE',
    Login_Start = 'LOGIN_START',
    Login_Fail = 'LOGIN_FAIL',
    Login_Finish = 'LOGIN_FINISH',
    Logout = 'LOGOUT',
    NotifyLoginStatusKnown = 'NOTIFY_LOGIN_STATUS_KNOWN',

    OpenProjectArchivationModal = 'OPEN_PROJECT_ARCHIVATION_MODAL',
    OpenProjectRevisionArchivationModal = 'OPEN_PROJECT_REVISION_ARCHIVATION_MODAL',
    CloseProjectArchivationModal = 'CLOSE_PROJECT_ARCHIVATION_MODAL',
    CloseProjectRevisionArchivationModal = 'CLOSE_PROJECT_REVISION_ARCHIVATION_MODAL',
    CloseCreateSnapshotModal = 'CLOSE_CREATE_PROJECT_REVISION_MODAL',
    OpenCreateProjectFromExistingSourcesModal = 'OPEN_CREATE_PROJECT_FROM_EXISTING_SOURCES_MODAL',
    CloseCreateProjectFromExistingSourcesModal = 'CLOSE_CREATE_PROJECT_FROM_EXISTING_SOURCES_MODAL',
    ChangeNewProjectName = 'CHANGE_NEW_PROJECT_NAME',
    ChangeNewProjectUrlSlug = 'CHANGE_NEW_PROJECT_URL_SLUG',
    ChangeNewProjectRevisionName = 'CHANGE_NEW_PROJECT_REVISION_NAME',
    ChangeNewProjectRevisionUrlSlug = 'CHANGE_NEW_PROJECT_REVISION_URL_SLUG',
    ToggleProjectArchivationList = 'TOGGLE_PROJECT_ARCHIVATION_LIST',
    ToggleProjectRevisionArchivationList = 'TOGGLE_PROJECT_REVISION_ARCHIVATION_LIST',
    ProcessExistingXsd_Fail = 'PROCESS_EXISTING_XSD_FAIL',
    ProcessExistingXsd_Finish = 'PROCESS_EXISTING_XSD_FINISH',
    ProcessExistingXsd_Start = 'PROCESS_EXISTING_XSD_START',
    RequireMissingXsdFiles = 'REQUIRE_MISSING_XSD_FILES',
    OpenCreateSnapshotModal = 'SHOW_CREATE_PROJECT_REVISION_DIALOG',

    CreateNewProject_Start = 'CREATE_NEW_PROJECT_START',
    CreateNewProject_Fail = 'CREATE_NEW_PROJECT_FAIL',
    CreateNewProject_Finish = 'CREATE_NEW_PROJECT_FINISH',

    CreateNewProjectRevision_Start = 'CREATE_NEW_PROJECT_REVISION_START',
    CreateNewProjectRevision_Fail = 'CREATE_NEW_PROJECT_REVISION_FAIL',
    CreateNewProjectRevision_Finish = 'CREATE_NEW_PROJECT_REVISION_FINISH',

    RestoreRevision_Start = 'RESTORE_REVISION_START',
    RestoreRevision_Fail = 'RESTORE_REVISION_FAIL',
    RestoreRevision_Finish = 'RESTORE_REVISION_FINISH',

    SetAutoGeneratePreview = 'SET_AUTO_GENERATE_PREVIEW',
    SetDomainItemMenuCollapsation = 'SET_DOMAIN_ITEM_MENU_COLLAPSATION',
    SetPreviewCollapsation = 'SET_PREVIEW_COLLAPSATION',
    ToggleDomainItemMenu = 'TOGGLE_DOMAIN_ITEM_MENU',
    OpenDownloadMenu = 'OPEN_DOWNLOAD_MENU',
    CloseDownloadMenu = 'CLOSE_DOWNLOAD_MENU',
    OpenExistingXsdsMenu = 'OPEN_EXISTING_XSDS_MENU',
    CloseExistingXsdsMenu = 'CLOSE_EXISTING_XSDS_MENU',
    OpenXsdsUploadMenu = 'OPEN_XSDS_UPLOAD_MENU',
    CloseXsdsUploadMenu = 'CLOSE_XSDS_UPLOAD_MENU',
    ChangeXsdsFileToUpload = 'CHANGE_XSDS_FILE_TO_UPLOAD',
    UploadExistingXsdsFileStart = 'UPLOAD_EXISTING_XSDS_FILE_START',
    UploadExistingXsdsFileSuccess = 'UPLOAD_EXISTING_XSDS_FILE_SUCCESS',
    UploadExistingXsdsFileFail = 'UPLOAD_EXISTING_XSDS_FILE_FAIL',

    InitSync_Start = 'INIT_SYNC_START',
    InitSync_Fail = 'INIT_SYNC_FAIL',
    InitSync_Finish = 'INIT_SYNC_FINISH',
    ChangeConnectionState = 'CHANGE_CONNECTION_STATE',

    NotifyIndexedDbAvailabilityKnown = 'NOTFIY_INDEXEDDB_AVAIALABILITY_KNOWN',
    NotifyLiveRevisionChanged = "NOTIFY_LIVE_REVISION_CHANGED",

    LoadValidation_Start = 'LOAD_VALIDATION_START',
    LoadValidation_Fail = 'LOAD_VALIDATION_FAIL',
    LoadValidation_Finish = 'LOAD_VALIDATION_FINISH',

    /*
     * This hints the TS compiler that there may be other actions
     * from other libraries not in this enum.
     */
    __NonExhaustive = '__NON_EXHAUSTIVE_MATCH__',
}

/**
 * Navigates to the given path relative to the current project.
 *
 * @param {string} path the path to navigate to, e.g. 'functions/4253432344-24343....'.
 * @param {boolean} preserveQuery preserve query parameters?
 * @returns {RouterActions} the push action
 */
export function pushInProject(path: string, preserveQuery: boolean = true): RouterActions {
    // Note: filter(val => val); filters our possibly empty strings produced if window.location.pathname contains //
    const [slug, projId, projRevId] = window.location.pathname.split('/').filter(val => val);
    path = path.replace(/^\/+/g, ''); // remove leading forward slash if present
    const p = `/${slug}/${projId}/${projRevId}/${path}`;
    return preserveQuery ? push(p) : routerPush(p);
}

/**
 * A location push that persists the query parameters by default.
 *
 * @param obj redux-little-router's push parameters
 * @param options redux-little-router's push options
 * @returns {RouterActions} the push action
 */
export function push(obj, options?): RouterActions {
    return routerPush(obj, Object.assign({persistQuery: true}, options));
}

export function pop(): Action {
    return goBack();
}
