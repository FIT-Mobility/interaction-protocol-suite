import { Function as Fn } from 'omp-schema';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';

import {
    addAssertion,
    editAssertion,
    nameChanged,
    removeAssertion,
    requestTypeChanged,
    responseTypeChanged,
} from '../../actions/components/editor/editor-functions';
import FunctionsEditor, {
    FunctionsEditorDispatch,
    FunctionsEditorProps,
} from '../../components/editor/editor-functions';
import { currentItemSelector, sortedElementsSelector } from '../../selectors/project';
import { State } from '../../state';

/**
 * Gets all the top level elements sorted by their creation date.
 */
const sortedTopLevelElementsSelector = createSelector(
    sortedElementsSelector,
    els => els.filter(el => el.isTopLevel),
);

const mapStateToProps = (state: State): FunctionsEditorProps => ({
    currentItem: currentItemSelector(state)[0] as Fn,
    elements: sortedTopLevelElementsSelector(state),
    disableEditing: state.components.disableEditing,
});
const mapDispatchToProps: FunctionsEditorDispatch = {
    addAssertion,
    changeAssertion: editAssertion,
    changeRequestType: requestTypeChanged,
    changeResponseType: responseTypeChanged,
    nameChanged,
    removeAssertion,
};

const Editor = connect(
    mapStateToProps,
    mapDispatchToProps,
)(FunctionsEditor);

export default Editor;
