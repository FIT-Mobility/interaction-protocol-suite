import { User } from '@ips/shared-js';
import AppBar from '@material-ui/core/es/AppBar';
import Button from '@material-ui/core/es/Button';
import CircularProgress from '@material-ui/core/es/CircularProgress';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import Toolbar from '@material-ui/core/es/Toolbar';
import Typography from '@material-ui/core/es/Typography';
import CompareArrows from '@material-ui/icons/CompareArrows';
import HourglassEmpty from '@material-ui/icons/HourglassEmpty';
import * as React from 'react';
import Helmet from 'react-helmet';
import { Fragment, Link } from 'redux-little-router';

import Editor from '../containers/editor';
import LiveRevisionChangeNotifier from '../containers/live-revision-change-notifier';
import Login from '../containers/login';
import ProjectRevisionsList from '../containers/project-revisions-list';
import ProjectsList from '../containers/projects-list';
import { ConnectionState, Sync } from '../state';

import { row } from './utils/flex-primitives';

export interface OmpAppProps {
    barTitle: string;
    currentUser: User;
    isIndexedDbAvailable: boolean;
    isNetworkConnected: boolean;
    loginStatusKnown: boolean;
    pageTitle: string;
    currentRevisionName: string | null;
    currentRevisionsPageUrl: string | null;
    isEditingDisabled: boolean;
    sync: Sync;
}

const styles = theme => createStyles({
    bar: {
        justifyContent: 'center',
        marginBottom: theme.spacing.unit * 2,
        textTransform: 'uppercase',
    },
    container: {
        display: 'flex',
        flexFlow: 'column nowrap',
        alignItems: 'center',
        width: '100%',
        height: '100%',
    },
    margin: {
        margin: theme.spacing.unit * 2,
    },
    registerLink: {
        color: 'white',
    },
    row,
    title: {
        marginRight: 'auto',
    },
    username: {
        '&:after': {
            content: '"|"', // Double quoting required here
            margin: '0 8px 0 8px',
        },
    },
});

const Body = (props: OmpAppProps & WithStyles<typeof styles>): any => {
    if (!props.isIndexedDbAvailable) {
        return (
            <div className={props.classes.margin}>
                <p>IndexedDB not available!</p>
                <p>Please leave incognito mode or use a browser that supports IndexedDB.</p>
            </div>
        );
    } else if (!props.loginStatusKnown || props.sync.initState === ConnectionState.Connecting) {
        return (
            <div className={props.classes.margin}>
                <CircularProgress/>
            </div>
        );
    } else if (props.sync.error) {
        return (
            <div className={props.classes.margin}>
                <p>Connecting to the backend failed:</p>
                <p>{props.sync.error.message}</p>
                <p>Please reload the site.</p>
            </div>
        );
    } else if (props.loginStatusKnown) {
        if (props.currentUser && props.sync.initState === ConnectionState.Connected) {
            return (
                <>
                    <Fragment forRoute="/projects/:projectUrlSlug/revisions">
                        <ProjectRevisionsList/>
                    </Fragment>
                    <Fragment forRoute="/projects/:projectUrlSlug/:revisionUrlSlug">
                        <Editor/>
                    </Fragment>
                    <Fragment forRoute="/">
                        <ProjectsList/>
                    </Fragment>
                    <LiveRevisionChangeNotifier/>
                </>
            );
        } else {
            return <Login/>;
        }
    } else {
        return null;
    }
};

const OmpApp = (props: OmpAppProps & WithStyles<typeof styles>) => (
    <Fragment forRoute="/">
        <div className={props.classes.container}>
            <Helmet>
                <title>{props.pageTitle}</title>
            </Helmet>

            <AppBar
                className={props.classes.bar}
                position="static"
                color="primary"
            >
                <Toolbar>
                    <Typography
                        variant="title"
                        color="inherit"
                    >
                        {props.barTitle}
                    </Typography>
                    {props.currentRevisionName && (
                        <div>
                            <Button color="inherit" href={props.currentRevisionsPageUrl!}>Revisions</Button>
                            <span> | Revision: {props.currentRevisionName} </span>
                            {props.isEditingDisabled && <span>Read-only!</span>}
                        </div>
                    )}
                    <span className={props.classes.title} />
                    {!props.currentUser && (
                        <div className={props.classes.row}>
                            <Link
                                href="/register"
                                className={props.classes.registerLink}
                            >
                                Register
                            </Link>
                        </div>
                    )}
                    {props.currentUser && (
                        <div className={props.classes.row}>
                            <Typography
                                variant="subheading"
                                color="inherit"
                                className={props.classes.username}
                            >
                                {props.currentUser.name}
                            </Typography>
                            {props.isNetworkConnected ? <CompareArrows/> : <HourglassEmpty/>}
                        </div>
                    )}
                </Toolbar>
            </AppBar>

            <Body {...props}/>
        </div>
    </Fragment>
);

export default withStyles(styles)(OmpApp);
