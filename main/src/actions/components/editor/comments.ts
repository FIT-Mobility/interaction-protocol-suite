import { ThunkAction } from 'redux-thunk';

import { push, Action, PayloadAction, Types } from '../..';
import { currentItemMetadataSelector } from '../../../selectors/project';
import { State } from '../../../state';
import { createComment as createCmt, deleteComment as delCmt } from '../../sync';

export type CommentsActions =
    | ChangeCommentTextAction
    | CloseCommentDeletionModalAction
    | OpenCommentDeletionModalAction;

export interface ChangeCommentTextAction extends PayloadAction<string> {
    type: Types.ChangeCommentText;
}
export interface OpenCommentDeletionModalAction extends PayloadAction<string> {
    type: Types.OpenCommentDeletionModal;
}
export interface CloseCommentDeletionModalAction {
    type: Types.CloseCommentDeletionModal;
}

export function changeCommentEditorContent(content: string): ChangeCommentTextAction {
    return {
        type: Types.ChangeCommentText,
        payload: content,
    };
}

export function closeDeletionModal(): CloseCommentDeletionModalAction {
    return { type: Types.CloseCommentDeletionModal };
}

export function createComment(): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        const s = getState();
        const [id] = currentItemMetadataSelector(s);
        if (!id) {
            throw new Error("Missing current item ID.");
        }
        if (!s.auth.currentUser) {
            throw new Error("Missing current user.");
        }

        dispatch(createCmt(
            id,
            s.auth.currentUser!,
            s.components.comments.commentText,
        ));
        dispatch(changeCommentEditorContent(''));
    };
}

export function deleteComment(): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        const commentId = getState().components.comments.commentToDelete;
        if (commentId) {
            dispatch(delCmt(commentId));
        }
        dispatch(closeDeletionModal());
    };
}

export function focusComment(commentId: string): Action {
    return push({ hash: commentId });
}

export function openDeletionModal(commentId: string): OpenCommentDeletionModalAction {
    return {
        type: Types.OpenCommentDeletionModal,
        payload: commentId,
    };
}
