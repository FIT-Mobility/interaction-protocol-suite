import { connect } from 'react-redux';

import {
    changeCommentEditorContent,
    closeDeletionModal,
    createComment,
    deleteComment,
} from '../../actions/components/editor/comments';
import CommentsView, { CommentaryDispatch, CommentaryProps } from '../../components/editor/commentary';
import { currentItemCommentsSelector } from '../../selectors/project';
import { State } from '../../state';

const mapStateToProps = (state: State): CommentaryProps => ({
    comments: currentItemCommentsSelector(state),
    commentText: state.components.comments.commentText,
    commentToDelete: state.components.comments.commentToDelete,
    currentUser: state.auth.currentUser!,
    deletionModalOpen: state.components.comments.deletionModalOpen,
    disableEditing: state.components.disableEditing,
});
const mapDispatchToProps: CommentaryDispatch = {
    changeCommentInputText: changeCommentEditorContent,
    closeDeletionModal,
    deleteComment,
    submitComment: createComment,
};

const Commentary = connect(
    mapStateToProps,
    mapDispatchToProps,
)(CommentsView);

export default Commentary;
