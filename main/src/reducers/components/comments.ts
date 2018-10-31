import { Action, Types } from '../../actions';
import { CommentsComponentState } from '../../state';

export default function(
    state: CommentsComponentState = {
        commentText: '',
        commentToDelete: null,
        deletionModalOpen: false,
    },
    action: Action,
): CommentsComponentState {
    switch (action.type) {
        case Types.ChangeCommentText:
            return {
                ...state,
                commentText: action.payload,
            };
        case Types.OpenCommentDeletionModal:
            return {
                ...state,
                commentToDelete: action.payload,
                deletionModalOpen: true,
            };
        case Types.CloseCommentDeletionModal:
            return {
                ...state,
                commentToDelete: null,
                deletionModalOpen: false,
            };
        default:
            return state;
    }
}
