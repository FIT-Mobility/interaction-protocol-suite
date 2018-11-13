import { DomainItemType } from "omp-schema";
import { connect } from 'react-redux';
import { createSelector } from 'reselect';

import { createCommentReference, onQuillTextChange } from '../../actions/components/editor/editor';
import EditorView, { EditorDispatch, EditorProps } from '../../components/editor/editor';
import {
    currentItemCommentsSelector,
    currentItemSelector,
    currentProjectRevisionSelector
} from '../../selectors/project';
import { EditorMode, State } from '../../state';

const editorModeSelector = createSelector(
    (s: State) => s.router.query.editor,
    mode => (mode && EditorMode[mode[0].toUpperCase() + mode.substr(1)]) || EditorMode.Definition,
);

const gerPlaceholder = (type: DomainItemType | null): string => {
    let ph: string;
    switch (type) {
        case DomainItemType.DataType : ph = "German data type documentation is not exported."; break;
        case DomainItemType.Function : ph = "German function documentation is not exported."; break;
        default : ph = "";
    }
    return ph;
};

const mapStateToProps = (state: State): EditorProps => {
    const [item, type] = currentItemSelector(state);
    return {
        commentCount: currentItemCommentsSelector(state).length,
        currentItem: item,
        currentItemType: type,
        currentProjectRevision: currentProjectRevisionSelector(state),
        editorMode: editorModeSelector(state),
        disableEditing: state.components.disableEditing,
        gerPlaceholder: gerPlaceholder(type),
    };
};
const mapDispatchToProps: EditorDispatch = {
    createCommentReference,
    onTextChange: onQuillTextChange,
};

const Editor = connect(
    mapStateToProps,
    mapDispatchToProps,
)(EditorView);

export default Editor;
