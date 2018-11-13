import { DomainItemType } from 'omp-schema';
import { connect } from 'react-redux';
import { createSelector } from 'reselect';

import {
    createItem,
    deleteItem,
    openItem,
    openProjectDocs,
    openXsdEditor,
    toggleMenu,
} from '../../actions/components/editor/domain-menu';
import DomainMenuView, { DomainMenuDispatch, DomainMenuProps } from '../../components/editor/domain-menu';
import {
    currentItemMetadataSelector,
    sortedDataTypesSelector,
    sortedFunctionsSelector,
    sortedSequencesSelector,
    sortedServicesSelector,
} from '../../selectors/project';
import { State } from '../../state';

const openMenuSelectorFactory = (domainItemType: DomainItemType) => createSelector(
    (s: State) => s.components.editor.openMenus,
    menus => menus.indexOf(domainItemType) !== -1,
);

const dataTypesMenuOpenSelector = openMenuSelectorFactory(DomainItemType.DataType);
const functionsMenuOpenSelector = openMenuSelectorFactory(DomainItemType.Function);
const sequencesMenuOpenSelector = openMenuSelectorFactory(DomainItemType.Sequence);
const servicesMenuOpenSelector = openMenuSelectorFactory(DomainItemType.Service);

const mapStateToProps = (state: State): DomainMenuProps => ({
    dataTypesMenuOpen: dataTypesMenuOpenSelector(state),
    functionsMenuOpen: functionsMenuOpenSelector(state),
    sequencesMenuOpen: sequencesMenuOpenSelector(state),
    servicesMenuOpen: servicesMenuOpenSelector(state),
    currentItemId : currentItemMetadataSelector(state)[0],

    dataTypes: sortedDataTypesSelector(state),
    functions: sortedFunctionsSelector(state),
    sequences: sortedSequencesSelector(state),
    services: sortedServicesSelector(state),
    disableEditing: state.components.disableEditing,
});
const mapDispatchToProps: DomainMenuDispatch = {
    createItem,
    deleteItem,
    openItem,
    openProjectDocs,
    openXsdEditor,
    toggleMenu,
};

const DomainMenu = connect(
    mapStateToProps,
    mapDispatchToProps,
)(DomainMenuView);

export default DomainMenu;
