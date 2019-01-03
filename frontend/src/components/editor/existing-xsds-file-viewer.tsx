import { ProjectRevision } from '@ips/shared-js';
import Button from '@material-ui/core/es/Button';
import Dialog from '@material-ui/core/es/Dialog';
import DialogActions from '@material-ui/core/es/DialogActions';
import DialogContent from '@material-ui/core/es/DialogContent';
import DialogTitle from '@material-ui/core/es/DialogTitle';
import IconButton from '@material-ui/core/es/IconButton';
import List from '@material-ui/core/es/List';
import ListItem from '@material-ui/core/es/ListItem';
import ListItemSecondaryAction from '@material-ui/core/es/ListItemSecondaryAction';
import ListItemText from '@material-ui/core/es/ListItemText';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import CloudDownload from '@material-ui/icons/CloudDownload';
import FileCopy from '@material-ui/icons/FileCopy';
import 'brace';
import 'brace/mode/text';
import 'brace/mode/xml';
import * as React from 'react';
import Ace from 'react-ace';
import { connect } from 'react-redux';
import { AutoSizer } from 'react-virtualized/dist/es/AutoSizer';
import { List as VirtualizedList } from 'react-virtualized/dist/es/List';
import { Fragment } from 'redux-little-router';

import { pushInProject } from '../../actions';
import { copyToClipboard } from '../../actions/components/editor/existing-xsds-file-viewer';
import { API_BACKEND_URL } from '../../config';
import { currentProjectRevisionSelector } from '../../selectors/project';
import { State } from '../../state';

export interface Props {
    currentProjectRevision: ProjectRevision;
    fileLoadError: Error | null;
    fileLoading: boolean;
    singleFile: string | null;
    singleFileContents: string | null;
    singleFileMode: string | null;
}

export interface Dispatch {
    close: () => void;
    closeFile: () => void;
    copyToClipboard: (text: string) => void;
    openFile: (index: number) => void;
}

const styles = createStyles({
    listItem: {
        textOverflow: 'wrap',
    },
    listItemText: {
        fontFamily: 'monospace',
    },
    modal: {
        alignItems: 'stretch',
    },
    paper: {
        maxWidth: '80vw',
    },
});

// tslint:disable:max-line-length
const Body = (props: Props & Dispatch & WithStyles<typeof styles>) => {
    if (props.singleFile) {
        return (
            <Ace
                mode={props.singleFileMode!}
                value={props.singleFileContents || ''}
                readOnly={true}
                width="80vw"
            />
        );
    } else if (props.fileLoading) {
        return (
            <>
                <p>Loading {props.singleFile}...</p>

                <Button onClick={props.closeFile}>Go back</Button>
            </>
        );
    } else if (props.fileLoadError) {
        return (
            <>
                <p>There was an error loading the requested file:</p>
                <p>{props.fileLoadError.message}</p>
                <p>Please try again.</p>

                <Button onClick={props.closeFile}>Go back</Button>
            </>
        );
    }

    const renderFileRow = ({ key, index, style }) => (
        <ListItem
            key={key}
            button={true}
            className={props.classes.listItem}
            onClick={() => props.openFile(index)}
            ContainerProps={{ style }}
        >
            <ListItemText
                classes={{ primary: props.classes.listItemText }}
                primary={props.currentProjectRevision.importableFilesList[index]}
            />

            <ListItemSecondaryAction>
                <IconButton
                    onClick={() => props.copyToClipboard(props.currentProjectRevision.importableFilesList[index])}
                    title="Copy file path to clipboard for easier importing in the XSD"
                >
                    <FileCopy/>
                </IconButton>
                <IconButton
                    component="a"
                    href={`${props.currentProjectRevision.baseUri}${props.currentProjectRevision.importableFilesList[index]}`}
                    title={`Download ${props.currentProjectRevision.importableFilesList[index]}.`}
                >
                    <CloudDownload/>
                </IconButton>
            </ListItemSecondaryAction>
        </ListItem>
    );

    return (
        <AutoSizer>
            {({ height, width }) => (
                <List>
                    <VirtualizedList
                        height={height}
                        width={width}
                        rowCount={props.currentProjectRevision.importableFilesList.length}
                        rowHeight={43}
                        rowRenderer={renderFileRow}
                    />
                </List>
            )}
        </AutoSizer>
    );
};

const Viewer = (props: Props & Dispatch & WithStyles<typeof styles>) => (
    <Fragment forRoute="/existing-files">
        <Dialog
            open={true}
            classes={{ paper: props.classes.paper, scrollPaper: props.classes.modal }}
            fullWidth={true}
            onClose={props.close}
        >
            <DialogTitle>
                {props.currentProjectRevision.importableFilesList.length} existing files
            </DialogTitle>
            <DialogContent>
                <Body {...props}/>
            </DialogContent>
            <DialogActions>
                {(!props.singleFile && !props.fileLoadError) && (
                    <Button
                        component="a"
                        href={`${API_BACKEND_URL}/content/${props.currentProjectRevision.importableFilesArchiveId}`}
                    >
                        Download all
                    </Button>
                )}
                {(props.singleFileContents || props.fileLoadError) &&
                    <Button onClick={props.closeFile}>Close File</Button>}
                <Button onClick={props.close}>Close</Button>
            </DialogActions>
        </Dialog>
    </Fragment>
);
// tslint:enable

const getMode = (fileName: string) => {
    const dotIdx = fileName.lastIndexOf('.');
    if (dotIdx < 0) {
        return 'text';
    }

    const ext = fileName.substr(fileName.lastIndexOf('.') + 1);
    return ((ext !== 'xsd') ? ext : 'xml') || 'text';
};

const mapStateToProps = (s: State): Props => ({
    currentProjectRevision: currentProjectRevisionSelector(s)!,
    fileLoadError: s.components.existingFilesEditor.fileLoadError,
    fileLoading: s.components.existingFilesEditor.isLoadingFile,
    singleFile: currentProjectRevisionSelector(s)!.importableFilesList[s.router!.params!.fileIndex],
    singleFileContents: s.components.existingFilesEditor.singleFileContents,
    singleFileMode: getMode(
        currentProjectRevisionSelector(s)!.importableFilesList[s.router!.params!.fileIndex] || ''
    ),
});

const mapDispatchToProps: Dispatch = {
    close: () => pushInProject('/'),
    closeFile: () => pushInProject('/existing-files'),
    copyToClipboard,
    openFile: (index: number) => pushInProject(`/existing-files/${index}`),
};

export default connect(
    mapStateToProps,
    mapDispatchToProps,
)(withStyles(styles)(Viewer));
