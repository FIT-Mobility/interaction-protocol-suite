import { connect } from 'react-redux';
import { createSelector } from 'reselect';

import { changeEditorMode } from '../../actions/components/editor/editor';
import TabView, { TabViewDispatch, TabViewOwnProps, TabViewProps } from '../../components/editor/editor-tab-view';
import { State } from '../../state';

const innerViewSelectorFactory = () => createSelector(
    (op: TabViewOwnProps) => op.activeTab,
    (op: TabViewOwnProps) => op.fallback,
    (op: TabViewOwnProps) => op.tabs,
    (activeTab, fallback, tabs) => {
        // tslint:disable-next-line:triple-equals
        const active = tabs && tabs.find(tab => tab.value == activeTab);
        return active ? active.inner : fallback;
    },
);

/*
 * Use factory function to create a unique memoizing selector for each instance to
 * ensure correct memoization.
 */
const mapStateToPropsFactory = () => {
    const innerViewSelector = innerViewSelectorFactory();
    return (state: State, ownProps: TabViewOwnProps): TabViewProps => ({
        activeTab: ownProps.activeTab,
        className: ownProps.className,
        inner: innerViewSelector(ownProps),
        tabs: ownProps.tabs,
    });
};
const mapDispatchToProps: TabViewDispatch = {
    changeEditorMode,
};

const Tabs = connect(
    mapStateToPropsFactory,
    mapDispatchToProps,
)(TabView);

export default Tabs;
