import Button from '@material-ui/core/es/Button';
import Checkbox from '@material-ui/core/es/Checkbox';
import Dialog from '@material-ui/core/es/Dialog';
import DialogActions from '@material-ui/core/es/DialogActions';
import DialogContent from '@material-ui/core/es/DialogContent';
import DialogContentText from '@material-ui/core/es/DialogContentText';
import DialogTitle from '@material-ui/core/es/DialogTitle';
import FormControlLabel from '@material-ui/core/es/FormControlLabel';
import IconButton from '@material-ui/core/es/IconButton';
import Input from '@material-ui/core/es/Input';
import Menu from '@material-ui/core/es/Menu';
import MenuItem from '@material-ui/core/es/MenuItem';
import Paper from '@material-ui/core/es/Paper';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import ChevronLeft from '@material-ui/icons/ChevronLeft';
import ChevronRight from '@material-ui/icons/ChevronRight';
import FileDownload from '@material-ui/icons/CloudDownload';
import FileUpload from '@material-ui/icons/CloudUpload';
import Code from '@material-ui/icons/Code';
import Description from '@material-ui/icons/Description';
import Eye from '@material-ui/icons/RemoveRedEye';
import classNames from 'classnames';
import { Project } from 'omp-schema';
import * as React from 'react';

import DomainMenu from '../../containers/editor/domain-menu';
import Editor from '../../containers/editor/editor';
import Preview from '../../containers/editor/preview';
import { hidden, row } from '../utils/flex-primitives';

import ExistingFilesEditor from './existing-xsds-file-viewer';

export function safe<T>(it: T | null | undefined, fn: (it: T) => string): string {
    return it ? fn(it) : '';
}

export interface EditorViewProps {
    autoGeneratePreview: boolean;
    currentProject: Project;
    existingXsdsFileUploadError: Error | null;
    existingXsdsFileUploadInProgress: boolean;
    hasExistingXsdFiles: boolean;
    isDomainItemMenuCollapsed: boolean;
    isDownloadMenuOpen: boolean;
    isExistingXsdsMenuOpen: boolean;
    isPreviewCollapsed: boolean;
    isUploadMenuOpen: boolean;
    disableEditing: boolean;
    downloadXsdLink: string;
}
export interface EditorViewDispatch {
    closeExistingXsdsMenu: () => void;
    closeDownloadMenu: () => void;
    closeUploadMenu: () => void;
    closeProject: () => void;
    downloadPdf: () => void;
    downloadDocx: () => void;
    openExistingXsdsMenu: () => void;
    openExistingXsdsViewer: () => void;
    openDownloadMenu: () => void;
    openUploadMenu: () => void;
    setAutoGeneratePreview: (autoGen: boolean) => void;
    setDomainItemMenuCollapsation: (collapse: boolean) => void;
    setPreviewCollapsation: (collapse: boolean) => void;
    uploadExistingXsdsFile: () => void;
    uploadInputChanged: (ev: React.ChangeEvent<HTMLInputElement>) => void;
}
export interface EditorWidths {
    menuOffset: number;
    previewOffset: number;
}
export interface EditorViewState extends EditorWidths {
    artifactsMenuAnchor: any;
    existingXsdsMenuAnchor: any;
    dragStartX: number | null;
    menuAnchor: any;
}

const styles = theme => createStyles({
    barButton: {
        display: 'flex',
        marginRight: theme.spacing.unit * 2,
    },
    barLabel: {
        '& span': {
            fontWeight: 500,
            textTransform: 'uppercase',
        },
    },
    barLink: {
        textDecoration: 'none',
    },
    barText: {
        marginLeft: theme.spacing.unit * 2,
    },
    closeThumb: {
        alignSelf: 'center',
        display: 'flex',
        alignItems: 'center',
        flexFlow: 'column nowrap',
    },
    collapseBtn: {
        margin: 0,
        padding: 0,
        width: theme.spacing.unit * 2,
    },
    editorBlock: {
        flex: '1 1 60%',
        overflow: 'auto',
    },
    editorBody: { // tslint:disable:object-literal-key-quotes
        alignSelf: 'stretch',
        display: 'grid',
        flex: 1,
        gridTemplateColumns: `${theme.spacing.unit * 32}px 24px 6fr 24px 4fr`,
        overflowY: 'hidden',
        padding: `0 ${theme.spacing.unit * 2}px ${theme.spacing.unit * 2}px ${theme.spacing.unit * 2}px`,
    },
    head: {
        alignSelf: 'stretch',
        flex: 'none',
        display: 'flex',
        flexDirection: 'row',
        flexShrink: 0,
        alignItems: 'center',
        marginBottom: theme.spacing.unit * 2,
        padding: theme.spacing.unit,
        width: '100%',
    },
    hidden,
    menuBlock: {
        flex: '1 1 30%',
        overflow: 'auto',
    },
    marginBottom: {
        marginBottom: theme.spacing.unit * 2,
    },
    previewBlock: {
        flex: '1 1 40%',
        overflow: 'auto',
    },
    row,
    thumb: {
        borderLeft: '2px solid rgba(0, 0, 0, .54)',
        borderRight: '2px solid rgba(0, 0, 0, .54)',
        cursor: 'pointer',
        height: `${theme.spacing.unit * 4}px`,
        margin: `0 ${theme.spacing.unit}px`,
        width: `${theme.spacing.unit / 2}px`,
    },

    /*
     * Styles that change the grid columns based on which parts
     * of the editor are currently collapsed.
     */
    bothHidden: {
        gridTemplateColumns: `${theme.spacing.unit * 3}px 1fr ${theme.spacing.unit * 3}px`,
    },
    menuHidden: {
        gridTemplateColumns: `${theme.spacing.unit * 3}px 6fr ${theme.spacing.unit * 3}px 4fr`,
    },
    previewHidden: {
        gridTemplateColumns: `${theme.spacing.unit * 32}px ${theme.spacing.unit * 3}px 6fr ${theme.spacing.unit * 3}px`,
    },
});

type Props =
    EditorViewProps &
    EditorViewDispatch &
    WithStyles<typeof styles> &
    { className?: string };

class EditorView extends React.Component<Props, EditorViewState> {
    private menuRef = React.createRef<typeof DomainMenu>();
    private previewRef = React.createRef<typeof Preview>();

    constructor(props: Props, context?: any) {
        super(props, context);

        this.state = {
            artifactsMenuAnchor: null,
            dragStartX: null,
            existingXsdsMenuAnchor: null,
            menuAnchor: null,
            menuOffset: 0,
            previewOffset: 0,
        };
    }

    /* tslint:disable:max-line-length */
    render() {
        const hiddenPartsClass = this.props.isDomainItemMenuCollapsed && this.props.isPreviewCollapsed
            ? this.props.classes.bothHidden
            : this.props.isDomainItemMenuCollapsed
                ? this.props.classes.menuHidden
                : this.props.isPreviewCollapsed
                    ? this.props.classes.previewHidden
                    : '';

        return (
            <>
                <Dialog
                    open={this.props.isUploadMenuOpen}
                    onClose={this.props.closeUploadMenu}
                >
                    <DialogTitle>Upload existing XSDs</DialogTitle>
                    <DialogContent>
                        <DialogContentText className={this.props.classes.marginBottom}>
                            Upload an archive file containing existing XSD files to the server. The archive will
                            be unpacked on the server and the files will be available for XSD importing in the XSD
                            editor as if they laid beside the XSD file.
                        </DialogContentText>

                        {this.props.existingXsdsFileUploadError &&
                            <DialogContentText className={this.props.classes.marginBottom}>
                                {this.props.existingXsdsFileUploadError.message}
                            </DialogContentText>}

                        <Input
                            fullWidth={true}
                            inputProps={{ accept: '.zip,.bzip2,.bz2,.tar,.tar.bz,.tar.gz'}}
                            onChange={this.props.uploadInputChanged}
                            type="file"
                        />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={this.props.closeUploadMenu}>
                            Cancel
                        </Button>
                        <Button
                            onClick={this.props.uploadExistingXsdsFile}
                            disabled={this.props.existingXsdsFileUploadInProgress}
                        >
                            Upload
                        </Button>
                    </DialogActions>
                </Dialog>

                <ExistingFilesEditor/>

                <Paper className={this.props.classes.head}>
                    <Button
                        className={this.props.classes.barButton}
                        onClick={this.props.closeProject}
                    >
                        <ChevronLeft/>
                        <span className={this.props.classes.barText}>Back to project list</span>
                    </Button>

                    <Button
                        className={this.props.classes.barButton}
                        onClick={this.handleExistingXsdsMenu}
                    >
                        <FileDownload/>
                        <span className={this.props.classes.barText}>Manage existing XSDs</span>
                    </Button>
                    <Menu
                        anchorEl={this.state.existingXsdsMenuAnchor}
                        open={this.props.isExistingXsdsMenuOpen}
                        onClose={this.props.closeExistingXsdsMenu}
                    >
                        <MenuItem
                            onClick={this.props.openExistingXsdsViewer}
                            disabled={!this.props.hasExistingXsdFiles}
                        >
                            <Eye/>
                            <span className={this.props.classes.barText}>View uploaded files</span>
                        </MenuItem>
                        <MenuItem
                            onClick={this.props.openUploadMenu}
                            className={this.props.disableEditing ? this.props.classes.hidden : ""}
                        >
                            <FileUpload/>
                            <span className={this.props.classes.barText}>Upload existing XSDs</span>
                        </MenuItem>
                    </Menu>

                    <Button
                        className={this.props.classes.barButton}
                        onClick={this.handleOpenDownloadMenu}
                    >
                        <FileDownload/>
                        <span className={this.props.classes.barText}>Download artifacts</span>
                    </Button>
                    <Menu
                        anchorEl={this.state.artifactsMenuAnchor}
                        open={this.props.isDownloadMenuOpen}
                        onClose={this.props.closeDownloadMenu}
                    >
                        <MenuItem>
                            <Code/>
                            <a
                                className={classNames(this.props.classes.barText, this.props.classes.barLink)}
                                href={this.props.downloadXsdLink}
                            >
                                Download .xsd
                            </a>
                        </MenuItem>
                        <MenuItem onClick={this.props.downloadPdf}>
                            <Description/>
                            <span className={this.props.classes.barText}>Download .pdf</span>
                        </MenuItem>
                        <MenuItem onClick={this.props.downloadDocx}>
                            <Description/>
                            <span className={this.props.classes.barText}>Download .docx</span>
                        </MenuItem>
                    </Menu>

                    <FormControlLabel
                        className={this.props.classes.barLabel}
                        label="Auto-generate Preview"
                        control={(
                            <Checkbox
                                checked={this.props.autoGeneratePreview}
                                onChange={ev => this.props.setAutoGeneratePreview(ev.target.checked)}
                                color="primary"
                            />
                        )}
                        title="Check and uncheck this to force regenerating the preview"
                    />
                </Paper>

                <main className={classNames(this.props.classes.editorBody, hiddenPartsClass)}>
                    <DomainMenu
                        className={classNames(
                            this.props.classes.menuBlock,
                            { [this.props.classes.hidden]: this.props.isDomainItemMenuCollapsed },
                        )}
                        ref={this.menuRef as any}
                    />

                    <div className={this.props.classes.closeThumb}>
                        <IconButton
                            className={this.props.classes.collapseBtn}
                            title={`${this.props.isDomainItemMenuCollapsed ? "Expand" : "Collapse"} Domain Item Menu`}
                            onClick={() => this.props.setDomainItemMenuCollapsation(!this.props.isDomainItemMenuCollapsed)}
                        >
                            {this.props.isDomainItemMenuCollapsed
                                ? <ChevronRight/>
                                : <ChevronLeft/>}
                        </IconButton>
                        <div
                            className={classNames(
                                this.props.classes.thumb,
                                { [this.props.classes.hidden]: this.props.isDomainItemMenuCollapsed },
                            )}
                            draggable={true}
                            onDrag={this.handleMenuDrag}
                            onMouseDown={this.handleMouseDown}
                            onMouseUp={this.handleMouseUp}
                            title="Adjust size"
                        />
                    </div>

                    <Editor className={this.props.classes.editorBlock}/>

                    <div className={this.props.classes.closeThumb}>
                        <IconButton
                            className={this.props.classes.collapseBtn}
                            title={`${this.props.isPreviewCollapsed ? "Expand" : "Collapse"} Preview`}
                            onClick={() => this.props.setPreviewCollapsation(!this.props.isPreviewCollapsed)}
                        >
                            {this.props.isPreviewCollapsed
                                ? <ChevronLeft/>
                                : <ChevronRight/>}
                        </IconButton>
                        <div
                            className={classNames(
                                this.props.classes.thumb,
                                { [this.props.classes.hidden]: this.props.isPreviewCollapsed },
                            )}
                            draggable={true}
                            onDrag={this.handlePreviewDrag}
                            onMouseDown={this.handleMouseDown}
                            onMouseUp={this.handleMouseUp}
                            title="Adjust size"
                        />
                    </div>

                    <Preview
                        className={classNames(
                            this.props.classes.previewBlock,
                            { [this.props.classes.hidden]: this.props.isPreviewCollapsed },
                        )}
                        ref={this.previewRef as any}
                    />
                </main>
            </>
        );
    }
    /* tslint:enable */

    private handleMenuDrag = (ev: React.DragEvent<HTMLDivElement>) => {
        const pageX = ev.pageX;
        this.setState(prevState => ({ menuOffset: pageX - (prevState.dragStartX || 0) }));
    }

    private handleMouseDown = (ev: React.MouseEvent<HTMLDivElement>) => {
        this.setState({ dragStartX: ev.pageX });
    }

    private handleMouseUp = () => {
        this.setState({ dragStartX: null });
    }

    private handlePreviewDrag = (ev: React.DragEvent<HTMLDivElement>) => {
        const pageX = ev.pageX;
        this.setState(prevState => ({ previewOffset: pageX - (prevState.dragStartX || 0) }));
    }

    private handleExistingXsdsMenu = (ev: React.MouseEvent<HTMLElement>) => {
        this.setState({ existingXsdsMenuAnchor: ev.currentTarget });
        this.props.openExistingXsdsMenu();
    }

    private handleOpenDownloadMenu = (ev: React.MouseEvent<HTMLElement>) => {
        this.setState({ artifactsMenuAnchor: ev.currentTarget });
        this.props.openDownloadMenu();
    }
}

export default withStyles(styles)(EditorView);
