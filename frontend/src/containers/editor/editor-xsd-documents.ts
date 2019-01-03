import schemaTree from '@ips/shared-js/schema-tree';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';

import { addAttribute, addNode } from '../../actions/components/editor/editor-xsd';
import XsdDocumentsEditor, {
    XsdDocumentsEditorDispatch,
    XsdDocumentsEditorProps,
} from '../../components/editor/editor-xsd-documents';
import { currentXsdRootElementSelector, xsdSelector } from '../../selectors/project';
import { State } from '../../state';

const focusedElementSelector = createSelector(
    (s: State) => s.components.xsdEditor.focusedElement,
    xsdSelector,
    (focused, xsd) => focused ? xsd[focused] : null,
);
const menuAttributesSelector = createSelector(
    focusedElementSelector,
    focused => focused
        ? schemaTree.elements[focused.type].attributes
        : [],
);
const menuElementsSelector = createSelector(
    focusedElementSelector,
    focused => focused
        ? schemaTree.elements[focused.type].children
        : [],
);

const mapStateToProps = (state: State): XsdDocumentsEditorProps => ({
    focusedElement: focusedElementSelector(state),
    menuAttributes: menuAttributesSelector(state),
    menuElements: menuElementsSelector(state),
    root: currentXsdRootElementSelector(state),
    schema: state.projectData.xsdSchema,
    disableEditing: state.components.disableEditing,
});
const mapDispatchToProps: XsdDocumentsEditorDispatch = {
    addAttribute,
    addNode,
};

const XsdEditor = connect(
    mapStateToProps,
    mapDispatchToProps,
)(XsdDocumentsEditor);

export default XsdEditor;
