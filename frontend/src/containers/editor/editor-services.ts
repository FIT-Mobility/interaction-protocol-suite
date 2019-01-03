import { Service } from '@ips/shared-js';
import { Dictionary } from 'lodash';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';

import {
    nameChanged,
    toggleFunctionInService,
    toggleSequenceInService,
} from '../../actions/components/editor/editor-services';
import ServicesEditor, { ServicesEditorDispatch, ServicesEditorProps } from '../../components/editor/editor-services';
import { currentItemSelector, sortedFunctionsSelector, sortedSequencesSelector } from '../../selectors/project';
import { State } from '../../state';

function toMap(list: string[]): Dictionary<boolean> {
    const ret = {};
    list.forEach(it => ret[it] = true);
    return ret;
}

const inServiceSelectorFactory = (sel: (s: Service) => string[]) => createSelector(
    currentItemSelector,
    ([svc]) => svc && toMap(sel(svc as Service)) || {},
);

const functionsInServiceSelector = inServiceSelectorFactory(svc => svc.functions);
const sequencesInServiceSelector = inServiceSelectorFactory(svc => svc.sequences);

const mapStateToProps = (state: State): ServicesEditorProps => ({
    currentItem: currentItemSelector(state)[0] as Service,
    functions: sortedFunctionsSelector(state),
    functionsInService: functionsInServiceSelector(state),
    sequences: sortedSequencesSelector(state),
    sequencesInService: sequencesInServiceSelector(state),
    disableEditing: state.components.disableEditing,
});
const mapDispatchToProps: ServicesEditorDispatch = {
    nameChanged,
    toggleFunction: toggleFunctionInService,
    toggleSequence: toggleSequenceInService,
};

const Editor = connect(
    mapStateToProps,
    mapDispatchToProps,
)(ServicesEditor);

export default Editor;
