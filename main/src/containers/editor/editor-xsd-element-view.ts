import { XsdAttribute } from 'omp-schema';
import { AttributeTypes, ElementTypes } from 'omp-schema/schema-tree';
import { connect } from 'react-redux';
import { bindActionCreators, Dispatch } from 'redux';
import { createSelector } from 'reselect';

import {
    addAttribute,
    editContent,
    focusNode,
    moveNode,
    removeNode,
} from '../../actions/components/editor/editor-xsd';
import DataTypesEditor, {
    XsdElementViewDispatch,
    XsdElementViewOwnProps,
    XsdElementViewProps,
} from '../../components/editor/editor-xsd-element-view';
import { xsdSelector } from '../../selectors/project';
import { State } from '../../state';

const attributeListSelectorFactory = () => createSelector(
    (s: State, op: XsdElementViewOwnProps) => op.element,
    el => Object.keys(el.attributes)
        .map(k => el.attributes[k])
        .sort((a, b) => a.position - b.position),
);

const canContainTextSelectorFactory = (attrSelector: (s: State, op: XsdElementViewOwnProps) => XsdAttribute[]) =>
    createSelector(
        (s: State, op: XsdElementViewOwnProps) => s.projectData.xsdSchema,
        (s: State, op: XsdElementViewOwnProps) => op.element,
        attrSelector,
        (schema, element, attrList) => {
            switch (element.name) {
                case schema.elements[ElementTypes.XS_AppInfo].name:
                case schema.elements[ElementTypes.XS_Documentation].name:
                    return true;
                case schema.elements[ElementTypes.XS_ComplexType].name:
                    const mixedAttribute = attrList.find(attr => attr.name === AttributeTypes.Mixed);

                    if (mixedAttribute && mixedAttribute.value === 'true') {
                        return true;
                    }
                    // fallthrough
                default:
                    return false;
            }
        },
    );

const elementTitleSelectorFactory = (attrSelector: (s: State, op: XsdElementViewOwnProps) => XsdAttribute[]) =>
    createSelector(
        attrSelector,
        (s: State, op: XsdElementViewOwnProps) => op.element.name,
        (attrs, name) => `<${name}${attrs.map(attr => ` ${attr.name}="${attr.value}"`).join('')}/>`,
    );

/*
 * Use factory function to create a unique memoizing selector for each instance to
 * ensure correct memoization.
 */
const mapStateToPropsFactory = () => {
    const attrSelector = attributeListSelectorFactory();
    const canContainTextSelector = canContainTextSelectorFactory(attrSelector);
    const titleSelector = elementTitleSelectorFactory(attrSelector);

    return (state: State, op: XsdElementViewOwnProps): XsdElementViewProps => ({
        attributes: attrSelector(state, op),
        canContainText: canContainTextSelector(state, op),
        element: op.element,
        index: op.index,
        isFocused: op.element.id === state.components.xsdEditor.focusedElement,
        title: titleSelector(state, op),
        validationError: state.components.xsdEditor.validationErrors[op.element.id] || null,
        xsd: xsdSelector(state),
        disableEditing: state.components.disableEditing,
    });
};
const mapDispatchToProps = (dispatch: Dispatch, ownProps: XsdElementViewOwnProps): XsdElementViewDispatch =>
    bindActionCreators({
        addAttribute,
        editContent: (ev: React.ChangeEvent<HTMLTextAreaElement>) => editContent(ownProps.element.id, ev.target.value),
        focusNode: () => focusNode(ownProps.element.id),
        moveNode,
        removeNode: () => removeNode(ownProps.element.id),
    }, dispatch);

const XsdElementView = connect(
    mapStateToPropsFactory,
    mapDispatchToProps,
)(DataTypesEditor);

export default XsdElementView as React.ComponentType<XsdElementViewOwnProps>;
