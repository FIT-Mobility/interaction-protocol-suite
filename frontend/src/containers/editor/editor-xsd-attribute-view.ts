import { connect } from 'react-redux';

import { editAttributeName, editAttributeValue, removeAttribute } from '../../actions/components/editor/editor-xsd';
import XsdAttrView, {
    XsdAttributeViewDispatch,
    XsdAttributeViewOwnProps,
    XsdAttributeViewProps,
} from '../../components/editor/editor-xsd-attribute-view';
import { State } from '../../state';

const mapStateToProps = (
    state: State,
    { attributeId, element, isFocused }: XsdAttributeViewOwnProps,
): XsdAttributeViewProps => ({
    attribute: element.attributes[attributeId],
    attributeId,
    element,
    isFocused,
    validationError: state.components.xsdEditor.validationErrors[attributeId] || null,
    disableEditing: state.components.disableEditing,
});
const mapDispatchToProps: XsdAttributeViewDispatch = {
    editName: editAttributeName,
    editValue: editAttributeValue,
    removeAttribute,
};

const XsdAttributeView = connect(
    mapStateToProps,
    mapDispatchToProps,
)(XsdAttrView);

export default XsdAttributeView;
