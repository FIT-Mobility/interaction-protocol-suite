import { connect } from 'react-redux';

import { focusComment, openDeletionModal } from '../../actions/components/editor/comments';
import CommentView, { CommentDispatch, CommentProps } from '../../components/editor/comment';
import { currentItemMetadataSelector } from '../../selectors/project';
import { State } from '../../state';

const mapStateToProps = (state: State, ownProps): CommentProps => {
    const [id] = currentItemMetadataSelector(state);
    const comment = state.projectData.comments[id!][ownProps.commentId];
    const user = state.auth.users[comment.createdBy];
    return {
        comment,
        initials: user.name.split(' ')
            .slice(0, 2)
            .map(part => part[0].toLocaleUpperCase())
            .join(''),
        isFromCurrentUser: user.id === state.auth.currentUser,
        user,
        disableEditing: state.components.disableEditing,
    };
};
const mapDispatchToProps: CommentDispatch = {
    focus: focusComment,
    openDeletionModal,
};

const Comment = connect(
    mapStateToProps,
    mapDispatchToProps,
)(CommentView);

export default Comment;
