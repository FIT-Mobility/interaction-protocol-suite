import { Sequence } from 'omp-schema';
import { connect } from 'react-redux';

import { nameChanged } from '../../actions/components/editor/editor-sequences';
import SequencesEditor, {
    SequencesEditorDispatch,
    SequencesEditorProps,
} from '../../components/editor/editor-sequences';
import { currentItemSelector } from '../../selectors/project';
import { State } from '../../state';

const mapStateToProps = (state: State): SequencesEditorProps => ({
    currentItem: currentItemSelector(state)[0] as Sequence,
    disableEditing: state.components.disableEditing,
});
const mapDispatchToProps: SequencesEditorDispatch = {
    nameChanged,
};

const Editor = connect(
    mapStateToProps,
    mapDispatchToProps,
)(SequencesEditor);

export default Editor;
