import { DataType } from 'omp-schema';
import { connect } from 'react-redux';

import DataTypesEditor, { DataTypesEditorProps } from '../../components/editor/editor-data-types';
import { currentItemSelector } from '../../selectors/project';
import { State } from '../../state';

const mapStateToProps = (state: State): DataTypesEditorProps => ({
    currentItem: currentItemSelector(state)[0]! as DataType,
});

const Editor = connect(mapStateToProps)(DataTypesEditor);

export default Editor;
