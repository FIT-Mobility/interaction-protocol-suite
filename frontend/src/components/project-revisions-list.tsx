// tslint:disable:object-literal-key-quotes
import { ProjectRevision } from '@ips/shared-js';
import Button from '@material-ui/core/es/Button';
import Collapse from '@material-ui/core/es/Collapse';
import Dialog from '@material-ui/core/es/Dialog';
import DialogActions from '@material-ui/core/es/DialogActions';
import DialogContent from '@material-ui/core/es/DialogContent';
import DialogContentText from '@material-ui/core/es/DialogContentText';
import DialogTitle from '@material-ui/core/es/DialogTitle';
import Divider from '@material-ui/core/es/Divider';
import IconButton from "@material-ui/core/es/IconButton";
import List from '@material-ui/core/es/List';
import ListItem from '@material-ui/core/es/ListItem';
import ListItemIcon from '@material-ui/core/es/ListItemIcon';
import ListItemSecondaryAction from '@material-ui/core/es/ListItemSecondaryAction';
import ListItemText from '@material-ui/core/es/ListItemText';
import ListSubheader from '@material-ui/core/es/ListSubheader';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import TextField from "@material-ui/core/es/TextField";
import Tooltip from '@material-ui/core/es/Tooltip';
import Archive from '@material-ui/icons/Archive';
import ChevronLeft from '@material-ui/icons/ChevronLeft';
import PlayArrow from '@material-ui/icons/PlayArrow';
import RemoveRedEye from '@material-ui/icons/RemoveRedEye';
import Restore from '@material-ui/icons/Restore';
import Save from '@material-ui/icons/Save';
import Unarchive from '@material-ui/icons/Unarchive';
import classNames from 'classnames';
import * as React from 'react';

import { CreateNewProjectRevisionError, CreateNewProjectRevisionErrorType, RestoreRevisionError } from "../state";

import Form from './utils/flex-paper';
import { row } from './utils/flex-primitives';

export interface ProjectRevisionsListProps {
    archivationModalOpen: boolean;
    archivedProjectRevisions: ProjectRevision[];
    archivedProjectRevisionsOpen: boolean;
    canCreateProjectRevision: boolean;
    error: CreateNewProjectRevisionError | RestoreRevisionError | null;
    isCreatingProjectRevision: boolean;
    isRestoringRevision: boolean;
    isNetworkConnected: boolean;
    createSnapshotModalOpen: boolean;
    currentLiveRevision: string;
    nameInput: string;
    projectRevisions: ProjectRevision[];
    projectRevisionToArchive: ProjectRevision;
    projectRevisionToClone: ProjectRevision;
    urlSlugInput: string;
    isInvalidRevisionURL: boolean;
}

export interface ProjectRevisionsListDispatch {
    archiveProjectRevision: (id: string) => void;
    closeArchivationModal: () => void;
    closeCreateSnapshotModal: () => void;
    closeProjectRevisionsOverview: () => void;
    createProjectRevision: (projectRevisionToClone: string, name: string, urlSlug: string) => void;
    nameInputChanged: (name: string) => void;
    openArchivationModal: (id: string) => void;
    openProjectRevision: (urlSlug: string) => void;
    openCreateSnapshotModal: (id: string) => void;
    restoreRevision: (id: string) => void;
    toggleArchivedProjectRevisionsList: () => void;
    unarchiveProjectRevision: (id: string) => void;
    urlSlugInputChanged: (name: string) => void;
}

interface ProjectRevisionProps {
    projRev: ProjectRevision;
    isLiveRev: boolean;
    isNetworkConnected: boolean;
    isReadOnly: boolean;
    open: (id: string) => any;
    action: (id: string) => any;
    createSnap: (id: string) => any;
    restoreRev: (id: string) => any;
}

const StatusIconStyle = {
    width: '25px',
};

const invisibleChar = '\u2063';

const ProjectRevision = ({
    projRev,
    open,
    action,
    createSnap,
    isLiveRev,
    isReadOnly,
    isNetworkConnected,
    restoreRev
}: ProjectRevisionProps) => (
    <ListItem button={true} onClick={() => open(projRev.urlSlug)}>
        <div style={StatusIconStyle}>
            { isLiveRev ? <ListItemIcon>
                 <PlayArrow style={{color: 'red'}}/>
            </ListItemIcon> : '' }
            { isReadOnly ? <ListItemIcon>
                <RemoveRedEye/>
            </ListItemIcon> : '' }
        </div>
        <ListItemText
            primary={projRev.name}
            secondary={`Created on ${new Date(projRev.createdOn).toLocaleString()}`}
        />

        <ListItemSecondaryAction>
            <Tooltip
                title="Restore Revision"
                enterDelay={1000}
            >
                <span>
                    <IconButton
                        disabled={isLiveRev || !isNetworkConnected}
                        onClick={() => restoreRev(projRev.id)}
                    >
                        <Restore/>
                    </IconButton>
                </span>
            </Tooltip>
            <Tooltip
                title="Create Snapshot"
                enterDelay={1000}
            >
                <span>
                    <IconButton
                        disabled={projRev.isArchived || isReadOnly || !isNetworkConnected}
                        onClick={() => createSnap(projRev.id)}
                    >
                        <Save/>
                    </IconButton>
                </span>
            </Tooltip>
            <Tooltip
                title={!projRev.isArchived ? 'Archive' : 'Unarchive'}
                enterDelay={1000}
            >
                <span>
                    <IconButton disabled={isLiveRev} onClick={() => action(projRev.id)}>
                        {!projRev.isArchived ? <Archive/> : <Unarchive/>}
                    </IconButton>
                </span>
            </Tooltip>
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
    barButton: {
        display: 'flex',
        marginRight: theme.spacing.unit * 2,
    },
    barText: {
        marginLeft: theme.spacing.unit * 2,
    },
    container: {
        justifyContent: 'start',
        '& > *': {
            flexShrink: 0,
        },
    },
    createProject: {},
    dialogHead: {
        justifyContent: 'flex-end',
        padding: theme.spacing.unit * 2,
    },
    head: {
        flex: 'none',
        display: 'flex',
        flexDirection: 'row',
        flexShrink: 0,
        alignItems: 'center',
        padding: theme.spacing.unit,
    },
    nameInput: {},
    urlSlugInput: {},
    row,
    '@media (max-width: 500px)': {
        createProject: {
            margin: 0,
            width: '100%',
        },
        dialogHead: {
            flexDirection: 'column',
        },
        urlSlugInput: {
            marginBottom: '10px',
            width: '100%',
        },
    },
});

type Props = ProjectRevisionsListProps &
    ProjectRevisionsListDispatch &
    WithStyles<typeof styles> &
    { className?: string };

/* tslint:disable:max-line-length */
const ProjectRevisionsList = ({ classes, ...props }: Props) => (
    <Form
        role="main"
        className={classNames(classes.container, props.className)}
    >
        <div className={classes.head}>
            <Button
                className={classes.barButton}
                onClick={props.closeProjectRevisionsOverview}
            >
                <ChevronLeft/>
                <span className={classes.barText}>Back to project</span>
            </Button>
        </div>

        <Dialog open={props.createSnapshotModalOpen} onClose={props.closeCreateSnapshotModal}>
            <DialogTitle>
                Create snapshot from Project Revision: {(props.projectRevisionToClone! || {}).name}
            </DialogTitle>
            <DialogContent>
                <div className={classNames(classes.dialogHead)}>
                    <div className={classNames(classes.row)}>
                        <TextField
                            className={classes.nameInput}
                            fullWidth={true}
                            label="Snapshot name"
                            onChange={((ev: any) => props.nameInputChanged(ev.target.value))}
                            onKeyPress={ev => {
                                if (ev.which === 13 && props.canCreateProjectRevision) {
                                    props.createProjectRevision(
                                        props.projectRevisionToClone!.id,
                                        props.nameInput,
                                        props.urlSlugInput
                                    );
                                }
                            }}
                            value={props.nameInput}
                        />
                    </div>
                    <div className={classes.row}>
                        <TextField
                            className={classes.urlSlugInput}
                            fullWidth={true}
                            label="URL"
                            onChange={((ev: any) => props.urlSlugInputChanged(ev.target.value))}
                            onKeyPress={ev => {
                                if (ev.which === 13 && props.canCreateProjectRevision) {
                                    props.createProjectRevision(
                                        props.projectRevisionToClone!.id,
                                        props.nameInput,
                                        props.urlSlugInput
                                    );
                                }
                            }}
                            value={props.urlSlugInput}
                            helperText={props.isInvalidRevisionURL ? "This URL is not allowed!" : invisibleChar}
                            error={props.isInvalidRevisionURL}
                        />
                    </div>
                </div>
                {props.error && (
                    <>
                        {props.error.cause === CreateNewProjectRevisionErrorType.Network &&
                            <span>
                                Could not reach server to create a new project revision.
                                Is your internet connection working?
                            </span>}
                        {props.error.cause === CreateNewProjectRevisionErrorType.UrlSlugNotUnique &&
                            <span>URL is already in use!</span>}
                        {props.error.cause === CreateNewProjectRevisionErrorType.UnexpectedServerError &&
                            <span>An unexpected error occurred on the server!</span>}
                    </>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={props.closeCreateSnapshotModal}>
                    Cancel
                </Button>
                <Button
                    onClick={() => props.createProjectRevision(
                        props.projectRevisionToClone!.id,
                        props.nameInput,
                        props.urlSlugInput
                    )}
                    disabled={!props.canCreateProjectRevision}
                    color="primary"
                >
                    Create Snapshot
                </Button>
            </DialogActions>
        </Dialog>

        <Dialog open={props.archivationModalOpen} onClose={props.closeArchivationModal}>
            <DialogTitle>
                Archiving Project Revision: {(props.projectRevisionToArchive || {}).name}
            </DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Are you sure you want to archive project revision "{(props.projectRevisionToArchive! || {}).name}"?
                    This can be undone later.
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={props.closeArchivationModal}>Cancel</Button>
                <Button
                    onClick={() => props.archiveProjectRevision(props.projectRevisionToArchive!.id)}
                    color="primary"
                >
                    Archive
                </Button>
            </DialogActions>
        </Dialog>

        <Divider/>

        <List>
            <ListSubheader>All Project Revisions</ListSubheader>

            {props.projectRevisions!.map(p => (
                <ProjectRevision
                    key={p.id}
                    projRev={p}
                    isLiveRev={p.id === props.currentLiveRevision!}
                    isNetworkConnected={props.isNetworkConnected}
                    isReadOnly={p.readOnly}
                    open={props.openProjectRevision!}
                    action={props.openArchivationModal!}
                    createSnap={props.openCreateSnapshotModal}
                    restoreRev={props.restoreRevision}
                />
            ))}
        </List>

        <Divider/>

        <List>
            <div className={classes.row}>
                <ListSubheader className={classes.archiveSubheader}>
                    Archived Project Revisions
                </ListSubheader>
                <Button
                    className={classes.archiveButton}
                    onClick={props.toggleArchivedProjectRevisionsList}
                >
                    {props.archivedProjectRevisionsOpen ? 'Close' : 'Open'}
                </Button>
            </div>

            <Collapse
                in={props.archivedProjectRevisionsOpen!}
                timeout="auto"
                unmountOnExit={true}
            >
                {props.archivedProjectRevisions!.map(projRev => (
                    <ProjectRevision
                        key={projRev.id}
                        projRev={projRev}
                        isLiveRev={false}
                        isNetworkConnected={props.isNetworkConnected}
                        isReadOnly={projRev.readOnly}
                        open={props.openProjectRevision!}
                        action={props.unarchiveProjectRevision!}
                        createSnap={props.openCreateSnapshotModal}
                        restoreRev={props.restoreRevision}
                    />
                ))}
            </Collapse>
        </List>
    </Form>
);
/* tslint:enable */

export default withStyles(styles)(ProjectRevisionsList);
