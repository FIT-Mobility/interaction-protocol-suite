import { Comment, User } from '@ips/shared-js';
import Av from '@material-ui/core/es/Avatar';
import Button from '@material-ui/core/es/Button';
import CardActions from '@material-ui/core/es/CardActions';
import CardContent from '@material-ui/core/es/CardContent';
import CardHeader from '@material-ui/core/es/CardHeader';
import Divider from '@material-ui/core/es/Divider';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import classNames from 'classnames';
import * as React from 'react';
import ReactQuill from 'react-quill';

import { API_BACKEND_URL } from '../../config';
import Card from '../utils/card';

export interface CommentProps {
    comment: Comment;
    initials: string;
    isFromCurrentUser: boolean;
    user: User | null;
    disableEditing: boolean;
}
export interface CommentDispatch {
    openDeletionModal: (commentId: string) => void;
    focus: (commentId: string) => void;
}

const Avatar = (props: CommentProps & CommentDispatch & { className?: string }) => {
    if (!props.user) {
        return null;
    }

    return props.user.avatar
        ? <Av src={`${API_BACKEND_URL}/content/${props.user.avatar}`}/>
        : <Av>{props.initials}</Av>;
};

export const commentQuillStyles = createStyles({
    '& .comment-reference': {
        cursor: 'pointer',
        textDecoration: 'underline',
        textDecorationStyle: 'dotted',
    },
    '& blockquote': {
        background: 'rgb(240, 240, 240)',
    }
});

const styles = theme => createStyles({
    root: {
        // tslint:disable-next-line:object-literal-key-quotes
        marginBottom: theme.spacing.unit * 2,
    },
    quill: commentQuillStyles,
});

type Props = CommentProps &
    CommentDispatch &
    WithStyles<typeof styles> &
    { className?: string, commentId?: string };

const Comment = (props: Props) => (
    <Card className={classNames(props.className, props.classes.root)} id={props.comment.id}>
        <CardHeader
            avatar={<Avatar {...props}/>}
            subheader={`Created on ${new Date(props.comment.createdOn).toLocaleString()}`}
            title={(props.user || { name: '' }).name}
        />
        <Divider/>
        <CardContent>
            <ReactQuill
                className={props.classes.quill}
                modules={{ toolbar: false }}
                readOnly={true}
                value={props.comment!.text}
            />
        </CardContent>
        {props.isFromCurrentUser && !props.disableEditing && <>
            <Divider/>
            <CardActions>
                <Button
                    size="small"
                    onClick={() => props.openDeletionModal(props.comment!.id)}
                >
                    Delete
                </Button>
            </CardActions>
        </>}
    </Card>
);

export default withStyles(styles)(Comment);
