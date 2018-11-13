/* tslint:disable:ban-types */

import { Dictionary } from 'lodash';
import {
    Comment,
    DataType,
    DataTypeDocumentation,
    DomainItemType,
    Element,
    Function,
    Project,
    ProjectRevision,
    Sequence,
    Service,
    User,
    XsdElement,
    XsdOperation,
} from 'omp-schema';
import { SchemaAnalysis } from 'omp-schema/xsd-analysis';

/**
 * The mode of the editor view.
 */
export enum EditorMode {
    /** The editor displays the comments section. */
    Comments = 'comments',

    /** The editor displays the view where the domain item's definition can be edited. */
    Definition = 'definition',

    /** The editor displays the view where the domain item's documentation can be edited. */
    Documentation = 'documentation',
}

/**
 * Represents an HTTP error.
 */
export interface HttpError extends Error {
    /** The response status code. */
    status?: number;
}

/**
 * Error state (any thrown but uncaught).
 */
export interface ErrorSnackbarState {
    isOpen: boolean;
    message: string | null;
}

/**
 * User auth state.
 */
export interface AuthState {
    currentUser: string | null;
    statusKnown: boolean;
    users: Record<string, User>;
}

/**
 * State subtree for the comment creation component.
 */
export interface CommentsComponentState {
    commentText: string;
    commentToDelete: string | null;
    deletionModalOpen: boolean;
}

/**
 * State subtree of the editor component.
 */
export interface EditorComponentState {
    existingXsdsFileUploadError: Error | null;
    existingXsdsFileUploadInProgress: boolean;
    isDomainMenuCollapsed: boolean;
    isDownloadMenuOpen: boolean;
    isExistingXsdsMenuOpen: boolean;
    isPreviewCollapsed: boolean;
    isUploadMenuOpen: boolean;
    openMenus: DomainItemType[];
    xsdsFileToUpload: File | null;
}

export interface ExistingFilesEditorComponentState {
    fileLoadError: Error | null;
    isLoadingFile: boolean;
    singleFileContents: string | null;
}

export interface LiveRevisionChangeNotifierComponentState {
    projectUrlSlugWithChangedLiveRevision: string;
}

/**
 * The reason why the login failed.
 */
export const enum LoginErrorType {
    /** The error was caused due to an invalid network connection. */
    Network,

    /** The error was caused due to invalid credentials provided to the login API. */
    Login,

    /** Error caused during registration (email already registered). */
    Registration,
}

/**
 * The reason why creating a new project failed.
 */
export const enum CreateNewProjectErrorType {
    /** The error was caused due to an invalid network connection. */
    Network,

    /** The error was caused due to a unique-constraint violation of the provided URL slug. */
    UrlSlugNotUnique,

    /** Something unexpected happened on the server. */
    UnexpectedServerError,
}

/**
 * The reason why creating a new project revision failed.
 */
export const enum CreateNewProjectRevisionErrorType {
    /** The error was caused due to an invalid network connection. */
    Network,

    /** The error was caused due to a unique-constraint violation of the provided URL slug. */
    UrlSlugNotUnique,

    /** Something unexpected happened on the server. */
    UnexpectedServerError,
}

/**
 * The reason why creating a new project revision failed.
 */
export const enum RestoreRevisionErrorType {
    /** The error was caused due to an invalid network connection. */
    Network,

    /** Something unexpected happened on the server. */
    UnexpectedServerError,
}

/**
 * Represents a login error.
 */
export interface LoginError extends HttpError {
    cause: LoginErrorType;
}

/**
 * Represents a create-project error.
 */
export interface CreateNewProjectError extends HttpError {
    cause: CreateNewProjectErrorType;
}

/**
 * Represents a create-project revision error.
 */
export interface CreateNewProjectRevisionError extends HttpError {
    cause: CreateNewProjectRevisionErrorType;
}

/**
 * Represents a restore revision error.
 */
export interface RestoreRevisionError extends HttpError {
    cause: RestoreRevisionErrorType;
}

/**
 * An XSD validation error.
 */
export interface ValidationError {
    column: number;
    line: number;
    message: string;
}

/**
 * State subtree of the login component.
 */
export interface LoginComponentState {
    error: LoginError | null;
    inProgress: boolean;
    message: string | null;
    userEmail: string;
    userPassword: string;
    isRegistering: boolean;
    userPasswordRepeat: string;
    userName: string;
}

/**
 * The mode of the preview view.
 */
export enum PreviewMode {
    /** The preview displays the view where the preview is shown. */
    Preview = 'preview',

    /** The preview displays the view where the changes are shown. */
    Changes = 'changes',
}

/**
 * State subtree of the preview component.
 */
export interface PreviewComponentState {
    autoGenerate: boolean;
    /**
     * Tuple consisting of the pdf and then the docx preview link.
     */
    previewLinks: [string, string];
    previewLoadError: HttpError | null;
    previewLoading: boolean;
}

/**
 * State subtree of the projects list component.
 */
export interface ProjectsListComponentState {
    archivationModalOpen: boolean;
    archivedProjectListOpen: boolean;
    createProjectFromExistingSourcesModalOpen: boolean;
    createProjectFromExistingSourcesError: Error | null;
    error: CreateNewProjectError | null;
    fileIdOfExistingXsd: string | null;
    isProcessingXsd: boolean;
    isCreateNewProjectInProgress: boolean;
    needsAdditionalFiles: string[] | null;
    nameInput: string;
    projectToArchive: string | null;
    urlSlugInput: string;
}

/**
 * State subtree of the project revisions list component.
 */
export interface ProjectRevisionsListComponentState {
    archivationModalOpen: boolean;
    archivedProjectRevisionsListOpen: boolean;
    createSnapshotModalOpen: boolean;
    error: CreateNewProjectRevisionError | RestoreRevisionError | null;
    isCreateNewProjectRevisionInProgress: boolean;
    isRestoreRevisionInProgress: boolean;
    nameInput: string;
    projectRevisionToArchive: string | null;
    projectRevisionToClone: string | null;
    urlSlugInput: string;
}

/**
 * State subtree of the XSD editor component.
 */
export interface XsdEditorComponentState {
    hasInitialDeltas: boolean;
    focusedElement: string | null;
    validationErrors: Dictionary<ValidationError[]>;
    validationInProgress: boolean;
    validationLoadError: Error | null;
}

/**
 * State subtree for all visual components.
 */
export interface Components {
    comments: CommentsComponentState;
    disableEditing: boolean;
    editor: EditorComponentState;
    existingFilesEditor: ExistingFilesEditorComponentState;
    errorSnackbar: ErrorSnackbarState;
    focusedComponent: string | null;
    login: LoginComponentState;
    liveRevisionChangeNotifier: LiveRevisionChangeNotifierComponentState;
    preview: PreviewComponentState;
    projectsList: ProjectsListComponentState;
    projectRevisionsList: ProjectRevisionsListComponentState;
    xsdEditor: XsdEditorComponentState;
}

/**
 * Data of the currently opened project.
 */
export interface ProjectData {
    /** Comments grouped by item ID -> comment ID */
    comments: Dictionary<Dictionary<Comment>>;
    dataTypes: Dictionary<DataType>;
    dataTypeDocumentations: Dictionary<DataTypeDocumentation>;
    elements: Dictionary<Element>;
    functions: Dictionary<Function>;
    sequences: Dictionary<Sequence>;
    services: Dictionary<Service>;
    xsd: Dictionary<XsdElement>;
    xsdAnalysis: SchemaAnalysis | null;
    xsdDeltas: XsdOperation[];
    xsdSchema: any;
}

/**
 * State subtree of the routing module.
 */
export interface RouterData {
    /** The current path name. */
    pathname: string;

    /** The currently active route. */
    route: string;

    /** Route parameters. */
    params?: Dictionary<string>;

    /** The previous router data. */
    previous?: RouterData;

    /** Query parameters. */
    query: Dictionary<string> & {
        editor?: EditorMode,
        preview?: PreviewMode,
    };

    /** The raw query. */
    search: string;
    result: Record<string, any>;
}

/**
 * The sync status.
 */
export const enum ConnectionState {
    Disconnected,
    Connecting,
    Connected,
}

/**
 * State subtree for the synchronization layer.
 */
export interface Sync {
    /** The current network state. */
    connectionState: ConnectionState;

    /** The availability of IndexedDB. */
    indexedDbAvailable: boolean;

    /** The sync initialization state. */
    initState: ConnectionState;

    /** Any errors that have occured during sync init. */
    error: Error | null;
}

/**
 * The app's state.
 */
export interface State {
    auth: AuthState;
    components: Components;
    projectData: ProjectData;
    projects: Dictionary<Project>;
    projectRevisions: Dictionary<ProjectRevision>;
    router: RouterData;
    sync: Sync;
}
