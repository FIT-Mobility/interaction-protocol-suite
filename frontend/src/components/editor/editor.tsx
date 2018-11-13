import Subheader from '@material-ui/core/es/ListSubheader';
import Paper from '@material-ui/core/es/Paper';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import ModeEdit from '@material-ui/icons/Edit';
import ModeComment from '@material-ui/icons/ModeComment';
import PieChart from '@material-ui/icons/PieChart';
import TabUnselected from '@material-ui/icons/TabUnselected';
import classNames from 'classnames';
import { DomainItems, DomainItemType, Language, OmpItem, ProjectRevision } from 'omp-schema';
import * as React from 'react';
import { Fragment } from 'redux-little-router';

import Commentary from '../../containers/editor/commentary';
import DataTypesEditor from '../../containers/editor/editor-data-types';
import FunctionsEditor from '../../containers/editor/editor-functions';
import SequencesEditor from '../../containers/editor/editor-sequences';
import ServicesEditor from '../../containers/editor/editor-services';
import TabView from '../../containers/editor/editor-tab-view';
import XsdDocumentsEditor from '../../containers/editor/editor-xsd-documents';
import { EditorMode } from '../../state';
import { column } from '../utils/flex-primitives';
import YQuill from '../utils/y-quill';

import { safe } from '.';

export interface EditorProps {
    commentCount: number;
    currentItem: DomainItems | null;
    currentItemType: DomainItemType | null;
    currentProjectRevision: ProjectRevision | null;
    editorMode: EditorMode;
    disableEditing: boolean;
    gerPlaceholder: string;
}
export interface EditorDispatch {
    createCommentReference: (refId: string, text: string) => void;
    onTextChange: () => void;
}

const styles = createStyles({
    centered: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        textAlign: 'center',
        height: '100%',
    },
    column,
    container: {
        position: 'relative',
    },
    count: {
        color: 'white',
        fontSize: '12px',
        position: 'absolute',
        textAlign: 'center',
        left: 0,
        right: 0,
        top: '3px',
    },
    definitionContainer: {
        alignItems: 'stretch',
        flexShrink: 0,
        padding: '16px',
    },
    divider: {
        backgroundColor: 'darkgray',
        margin: '16px 0',
    },
    tabSplit: {
        display: 'grid',
        gridColumnGap: '8px',
        gridTemplateColumns: '1fr 2px 1fr',
        height: '100%',
    },
});

const CommentIconView = (props: { count: number } & WithStyles<typeof styles>) => (
    <div className={props.classes.container}>
        <span className={props.classes.count}>{props.count >= 100 ? '99+' : props.count}</span>
        <ModeComment/>
    </div>
);
const CommentIcon = withStyles(styles)(CommentIconView);

type TabsProps = EditorProps &
    EditorDispatch  &
    WithStyles<typeof styles> &
    { className?: string };

const TabsView = (props: TabsProps & { inner: React.ReactNode }) => {
    const documentationBody = (
        <div className={props.classes.tabSplit}>
            <div className={props.classes.column}>
                <Subheader>German</Subheader>
                <YQuill
                    richtextId={
                        safe(
                            props.currentItem as OmpItem,
                            (it: OmpItem) => it.docText ? it.docText[Language.German] : '',
                        )
                    }
                    disableEditing={props.disableEditing}
                    onCreateReference={props.createCommentReference}
                    onTextChange={props.onTextChange}
                    placeholder={props.gerPlaceholder}
                />
            </div>
            <div className={props.classes.divider}/>
            <div className={props.classes.column}>
                <Subheader>English</Subheader>
                <YQuill
                    richtextId={
                        safe(
                            props.currentItem as OmpItem,
                            (it: OmpItem) => it.docText ? it.docText[Language.English] : '',
                        )
                    }
                    disableEditing={props.disableEditing}
                    onCreateReference={props.createCommentReference}
                    onTextChange={props.onTextChange}
                />
            </div>
        </div>
    );
    const tabCfg = [{
        label: "Definition",
        icon: <PieChart/>,
        inner: (
            <div className={classNames(props.classes.column, props.classes.definitionContainer)}>
                {props.inner}
            </div>
        ),
        value: EditorMode.Definition,
    }, {
        label: "Documentation",
        icon: <ModeEdit/>,
        inner: documentationBody,
        value: EditorMode.Documentation,
    }, {
        label: "Comments",
        icon: <CommentIcon count={props.commentCount}/>,
        inner: <Commentary/>,
        value: EditorMode.Comments,
    }];

    return (
        <TabView
            className={props.className}
            activeTab={props.editorMode}
            tabs={tabCfg}
        />
    );
};
const Tabs = withStyles(styles)(TabsView);

const UnselectedView = (props: WithStyles<typeof styles> & { className?: string }) => (
    <Paper className={props.className}>
        <div className={props.classes.centered}>
            <TabUnselected/>
            <Subheader>Please select an item from the sidebar to edit</Subheader>
        </div>
    </Paper>
);
const Unselected = withStyles(styles)(UnselectedView);

// Some weird typings error :(
const XsdDocEditor: any = XsdDocumentsEditor;

const ProjectView = (props: TabsProps) => {
    const documentationBody = (
        <div className={props.classes.tabSplit}>
            <div className={props.classes.column}>
                <Subheader>German</Subheader>
                <YQuill
                    className={props.className}
                    richtextId={props.currentProjectRevision!.docText[Language.German]}
                    disableEditing={props.disableEditing}
                    onTextChange={props.onTextChange}
                />
            </div>
            <div className={props.classes.divider}/>
            <div className={props.classes.column}>
                <Subheader>English</Subheader>
                <YQuill
                    className={props.className}
                    richtextId={props.currentProjectRevision!.docText[Language.English]}
                    disableEditing={props.disableEditing}
                    onTextChange={props.onTextChange}
                />
            </div>
        </div>
    );
    const tabCfg = [{
        label: "Documentation",
        icon: <ModeEdit/>,
        inner: documentationBody,
        value: EditorMode.Documentation,
    }, {
        label: "Comments",
        icon: <CommentIcon count={props.commentCount}/>,
        inner: <Commentary/>,
        value: EditorMode.Comments,
    }];

    return (
        <TabView
            className={props.className}
            activeTab={
                props.editorMode === EditorMode.Definition
                    ? EditorMode.Documentation
                    : props.editorMode
            }
            tabs={tabCfg}
        />
    );
};
const Projects = withStyles(styles)(ProjectView);

// We use a class to be able to render an array
class EditorBlock extends React.PureComponent<
    EditorProps & EditorDispatch & { className?: string }
> {
    render() {
        return (
            <>
                <Fragment forRoute="/data-types/:datatypeId">
                    <Tabs {...this.props} inner={<DataTypesEditor/>}/>
                </Fragment>
                <Fragment forRoute="/functions/:functionId">
                    <Tabs {...this.props} inner={<FunctionsEditor/>}/>
                </Fragment>
                <Fragment forRoute="/sequences/:sequenceId">
                    <Tabs {...this.props} inner={<SequencesEditor/>}/>
                </Fragment>
                <Fragment forRoute="/services/:serviceId">
                    <Tabs {...this.props} inner={<ServicesEditor/>}/>
                </Fragment>
                <Fragment forRoute="/project">
                    <Projects {...this.props}/>
                </Fragment>
                <Fragment forRoute="/xsd">
                    <XsdDocEditor className={this.props.className}/>
                </Fragment>
                <Fragment forRoute="/">
                    <Unselected className={this.props.className}/>
                </Fragment>
            </>
        );
    }
}

export default EditorBlock;
