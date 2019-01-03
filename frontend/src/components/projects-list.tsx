// tslint:disable:object-literal-key-quotes

import { Project } from '@ips/shared-js';
import Button from '@material-ui/core/es/Button';
import CircularProgress from '@material-ui/core/es/CircularProgress';
import Collapse from '@material-ui/core/es/Collapse';
import Dialog from '@material-ui/core/es/Dialog';
import DialogActions from '@material-ui/core/es/DialogActions';
import DialogContent from '@material-ui/core/es/DialogContent';
import DialogContentText from '@material-ui/core/es/DialogContentText';
import DialogTitle from '@material-ui/core/es/DialogTitle';
import Divider from '@material-ui/core/es/Divider';
import IconButton from '@material-ui/core/es/IconButton';
import Input from '@material-ui/core/es/Input';
import LinearProgress from '@material-ui/core/es/LinearProgress';
import List from '@material-ui/core/es/List';
import ListItem from '@material-ui/core/es/ListItem';
import ListItemSecondaryAction from '@material-ui/core/es/ListItemSecondaryAction';
import ListItemText from '@material-ui/core/es/ListItemText';
import ListSubheader from '@material-ui/core/es/ListSubheader';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import TextField from '@material-ui/core/es/TextField';
import Archive from '@material-ui/icons/Archive';
import Unarchive from '@material-ui/icons/Unarchive';
import classNames from 'classnames';
import * as React from 'react';

import { CreateNewProjectError, CreateNewProjectErrorType } from '../state';

import Form from './utils/flex-paper';
import { column, row } from './utils/flex-primitives';

export interface ProjectsListProps {
    archivationModalOpen: boolean;
    archivedProjects: Project[];
    archivedProjectsOpen: boolean;
    canCreateProject: boolean;
    canCreateProjectFromExistingSources: boolean;
    error: CreateNewProjectError | null;
    existingSourcesModalOpen: boolean;
    isCreateNewProjectInProgress: boolean;
    isProcessingUploadedXsd: boolean;
    nameInput: string;
    needsAdditionalFiles: string[] | null;
    projects: Project[];
    projectToArchive: Project;
    urlSlugInput: string;
    xsdProcessingError: Error | null;
}

export interface ProjectsListDispatch {
    archiveProject: (id: string) => void;
    closeArchivationModal: () => void;
    closeCreateFromExistingSourcesModal: () => void;
    createProject: (name: string, urlSlug: string) => void;
    createProjectFromExistingSources: () => void;
    openCreateFromExistingSourcesModal: (name: string) => void;
    nameInputChanged: (name: string) => void;
    urlSlugInputChanged: (name: string) => void;
    openArchivationModal: (id: string) => void;
    openProject: (urlSlug: string) => void;
    xsdFileInputChanged: (el: HTMLInputElement) => void;
    toggleArchivedProjectsList: () => void;
    unarchiveProject: (id: string) => void;
}

interface ProjectProps {
    proj: Project;
    open: (id: string) => any;
    action: (id: string) => any;
}

const createExistingStyles = theme => createStyles({
    additionalFileList: {
        listDecoration: 'none',
    },
    additionalFileItem: {
        listDecoration: 'none',
    },
    column,
    content: {
        alignItems: 'stretch',
        width: 500,
    },
    error: {
        color: 'red',
    },
    inputDescription: {
        marginRight: theme.spacing.unit * 2,
        whiteSpace: 'nowrap',
    },
    progress: {
        alignSelf: 'center',
        margin: theme.spacing.unit * 2,
    },
    row,
});

type ExistingStylesProps =
    ProjectsListProps &
    ProjectsListDispatch &
    WithStyles<typeof createExistingStyles>;

const CreateFromExistingSourcesDialogView: React.SFC<ExistingStylesProps> = (props) => (
    <Dialog
        open={props.existingSourcesModalOpen}
        onClose={props.closeCreateFromExistingSourcesModal}
    >
        <DialogTitle>Creating Project from existing XSD sources: {props.nameInput}</DialogTitle>
        <DialogContent className={classNames(props.classes.content, props.classes.column)}>
            <div className={props.classes.row}>
                <label
                    htmlFor="uploadRootXsd"
                    className={props.classes.inputDescription}
                >
                    Select root XSD File:
                </label>
                <Input
                    id="uploadRootXsd"
                    disabled={props.isProcessingUploadedXsd}
                    fullWidth={true}
                    inputProps={{ accept: '.xml,.xsd' }}
                    onChange={ev => props.xsdFileInputChanged(ev.target as HTMLInputElement)}
                    type="file"
                />
            </div>

            {props.isProcessingUploadedXsd &&
                <CircularProgress
                    color="primary"
                    className={props.classes.progress}
                    title="Processing XSD files"
                />
            }

            {props.xsdProcessingError &&
                <p className={props.classes.error}>
                    {props.xsdProcessingError.message}
                </p>
            }
        </DialogContent>
        <DialogActions>
            <Button onClick={props.closeCreateFromExistingSourcesModal}>Cancel</Button>
            <Button
                color="primary"
                disabled={!props.canCreateProjectFromExistingSources}
                onClick={props.createProjectFromExistingSources}
            >
                Create
            </Button>
        </DialogActions>
    </Dialog>
);
const CreateFromExistingSourcesDialog = withStyles(createExistingStyles)(CreateFromExistingSourcesDialogView);

const Project: React.SFC<ProjectProps> = ({ proj, open, action }) => (
    <ListItem button={true} onClick={() => open(proj.urlSlug)}>
        <ListItemText
            primary={proj.name}
            secondary={`Created on ${new Date(proj.createdOn).toLocaleString()}`}
        />

        <ListItemSecondaryAction>
            <IconButton onClick={() => action(proj.id)}>
                {!proj.isArchived ? <Archive/> : <Unarchive/>}
            </IconButton>
        </ListItemSecondaryAction>
    </ListItem>
);

const styles = theme => createStyles({
    archiveButton: {
        marginLeft: 'auto',
        marginRight: theme.spacing.unit * 2,
    },
    archiveSubheader: {
        display: 'inline-block',
    },
    container: {
        justifyContent: 'start',
        '& > *': {
            flexShrink: 0,
        },
    },
    createProject: {},
    createProjectFromExistingSourcesDialog: {
        width: '500px',
    },
    head: {
        justifyContent: 'flex-end',
        padding: theme.spacing.unit * 2,
    },
    nameInput: {},
    urlSlugInput: {},
    row,
    '@media (max-width: 500px)': {
        createProject: {
            margin: 0,
            width: '100%',
        },
        head: {
            flexDirection: 'column',
        },
        urlSlugInput: {
            marginBottom: '10px',
            width: '100%',
        },
    },
});

type Props = ProjectsListProps &
    ProjectsListDispatch &
    WithStyles<typeof styles> &
    { className?: string };

/* tslint:disable:max-line-length */
const ProjectsList: React.SFC<Props> = ({ classes, ...props }) => (
    <Form
        role="main"
        className={classNames(classes.container, props.className)}
    >
        <Dialog open={props.archivationModalOpen} onClose={props.closeArchivationModal}>
            <DialogTitle>Archiving Project: {(props.projectToArchive! || {}).name}</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Are you sure you want to archive project "{(props.projectToArchive! || {}).name}"? This can be
                    undone later.
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={props.closeArchivationModal}>Cancel</Button>
                <Button
                    onClick={() => props.archiveProject(props.projectToArchive!.id)}
                    color="primary"
                >
                    Archive
                </Button>
            </DialogActions>
        </Dialog>

        <CreateFromExistingSourcesDialog {...props}/>

        <div className={classNames(classes.head)}>
            <div className={classNames(classes.row)}>
                <TextField
                    className={classes.nameInput}
                    fullWidth={true}
                    label="Project Name"
                    onChange={((ev: any) => props.nameInputChanged(ev.target.value))}
                    onKeyPress={ev => {
                        if (ev.which === 13 && props.canCreateProject) {
                            props.createProject(props.nameInput, props.urlSlugInput);
                        }
                    }}
                    value={props.nameInput}
                />
                <Button
                    className={classes.createProject}
                    fullWidth={true}
                    disabled={!props.canCreateProject}
                    onClick={() => props.createProject(props.nameInput, props.urlSlugInput)}
                    color="primary"
                >
                    Create new project
                </Button>
                <Button
                    className={classes.createProject}
                    fullWidth={true}
                    disabled={!props.canCreateProject}
                    onClick={() => props.openCreateFromExistingSourcesModal(props.nameInput)}
                    color="primary"
                >
                    Create Project from existing XSD sources
                </Button>
            </div>
            <div className={classNames(classes.row)}>
                <TextField
                    className={classes.urlSlugInput}
                    fullWidth={true}
                    label="URL"
                    onChange={((ev: any) => props.urlSlugInputChanged(ev.target.value))}
                    onKeyPress={ev => {
                        if (ev.which === 13 && props.canCreateProject) {
                            props.createProject(props.nameInput, props.urlSlugInput);
                        }
                    }}
                    value={props.urlSlugInput}
                />
            </div>
        </div>

        {props.isCreateNewProjectInProgress && <LinearProgress/>}

        {props.error && (
            <>
                {props.error.cause === CreateNewProjectErrorType.Network &&
                    <span>Could not reach server to create a new project. Is your internet connection working?</span>}
                {props.error.cause === CreateNewProjectErrorType.UrlSlugNotUnique &&
                    <span>URL is already in use!</span>}
                {props.error.cause === CreateNewProjectErrorType.UnexpectedServerError &&
                    <span>An unexpected error occurred on the server!</span>}
            </>
        )}

        <Divider/>

        <List>
            <ListSubheader>All Projects</ListSubheader>

            {props.projects!.map(p => (
                <Project
                    key={p.id}
                    proj={p}
                    open={props.openProject!}
                    action={props.openArchivationModal!}
                />
            ))}
        </List>

        <Divider/>

        <List>
            <div className={classes.row}>
                <ListSubheader className={classes.archiveSubheader}>Archived Projects</ListSubheader>
                <Button
                    className={classes.archiveButton}
                    onClick={props.toggleArchivedProjectsList}
                >
                    {props.archivedProjectsOpen ? 'Close' : 'Open'}
                </Button>
            </div>

            <Collapse
                in={props.archivedProjectsOpen!}
                timeout="auto"
                unmountOnExit={true}
            >
                {props.archivedProjects!.map(proj => (
                    <Project
                        key={proj.id}
                        proj={proj}
                        open={props.openProject}
                        action={props.unarchiveProject}
                    />
                ))}
            </Collapse>
        </List>

        <Divider/>

        <ListSubheader>Version: {process.env.COMMIT_ID || 'HEAD'}</ListSubheader>
    </Form>
);
/* tslint:enable */

export default withStyles(styles)(ProjectsList);
