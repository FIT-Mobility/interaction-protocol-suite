import { Comment } from '@ips/shared-js';
import Button from '@material-ui/core/es/Button';
import CardActions from '@material-ui/core/es/CardActions';
import CardContent from '@material-ui/core/es/CardContent';
import CardHeader from '@material-ui/core/es/CardHeader';
import Dialog from '@material-ui/core/es/Dialog';
import DialogActions from '@material-ui/core/es/DialogActions';
import DialogContent from '@material-ui/core/es/DialogContent';
import DialogContentText from '@material-ui/core/es/DialogContentText';
import DialogTitle from '@material-ui/core/es/DialogTitle';
import Divider from '@material-ui/core/es/Divider';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import classNames from 'classnames';
import * as React from 'react';
import * as ReactQuill from 'react-quill';

import CommentView from '../../containers/editor/comment';
import Card from '../utils/card';
import { hidden } from '../utils/flex-primitives';

import { commentQuillStyles } from './comment';

export interface CommentaryProps {
    comments: Comment[];
    commentText: string;
    commentToDelete: string | null;
    currentUser: string;
    deletionModalOpen: boolean;
    disableEditing: boolean;
}
export interface CommentaryDispatch {
    changeCommentInputText: (text: string) => void;
    closeDeletionModal: () => void;
    deleteComment: () => void;
    submitComment: () => void;
}

/* tslint:disable:object-literal-key-quotes */
const quillModules = {
    toolbar: [
        [{ 'header': [1, 2, false] }],
        [{ 'list': 'ordered'}, { 'list': 'bullet' }],
        ['bold', 'italic', 'underline', 'strike', 'blockquote', 'code-block'],
        ['link', 'image', 'clean'],
    ],
};
/* tslint:enable */

const styles = theme => createStyles({
    container: {
        padding: `${theme.spacing.unit * 2}px`,
    },
    create: {
        marginBottom: `${theme.spacing.unit * 2}px`,
    },
    hidden,
    quill: {
        ...commentQuillStyles,
        '& .ql-editor': {
            height: 150,
        },
    },
});

type Props = CommentaryProps &
    CommentaryDispatch &
    WithStyles<typeof styles> &
    { className?: string };

const Commentary = (props: Props) => (
    <div className={classNames(props.classes.container, props.className)}>
        <Dialog open={props.deletionModalOpen} onClose={props.closeDeletionModal}>
            <DialogTitle>Deleting comment</DialogTitle>
            <DialogContent>
                <DialogContentText>
                    Are you sure you want to delete your comment? This cannot be undone.
                </DialogContentText>
            </DialogContent>
            <DialogActions>
                <Button onClick={props.closeDeletionModal}>
                    Cancel
                </Button>
                <Button
                    onClick={props.deleteComment!}
                    color="primary"
                >
                    Delete
                </Button>
            </DialogActions>
        </Dialog>

        {props.comments!.map(cmt => <CommentView key={cmt.id} commentId={cmt.id}/>)}

        <Card className={props.disableEditing ? props.classes.hidden : props.classes.create}>
            <CardHeader title="Create new comment"/>
            <Divider/>
            <CardContent>
                <ReactQuill
                    className={props.classes.quill}
                    id="comment-editor"
                    value={props.commentText!}
                    modules={quillModules}
                    onChange={props.changeCommentInputText!}
                    placeholder={"Comments are only saved after being submitted."}
                />
            </CardContent>
            <Divider/>
            <CardActions>
                <Button
                    color="primary"
                    size="small"
                    onClick={props.submitComment}
                >
                    Submit
                </Button>
            </CardActions>
        </Card>
    </div>
);

export default withStyles(styles)(Commentary);
