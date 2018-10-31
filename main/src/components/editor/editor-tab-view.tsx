import AppBar from '@material-ui/core/es/AppBar';
import Paper from '@material-ui/core/es/Paper';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import Tab, { TabProps } from '@material-ui/core/es/Tab';
import Tabs from '@material-ui/core/es/Tabs';
import classNames from 'classnames';
import * as React from 'react';

import { EditorMode } from '../../state';

export interface TabConfig extends TabProps {
    inner: React.ReactNode;
}

export interface TabViewProps {
    activeTab?: any;
    className?: string;
    inner: React.ReactNode;
    tabs?: TabConfig[] | null;
}
export interface TabViewOwnProps {
    activeTab?: any;
    className?: string;
    fallback?: React.ReactNode;
    tabs?: TabConfig[] | null;
}
export interface TabViewDispatch {
    changeEditorMode: (mode: EditorMode) => void;
}

const styles = createStyles({
    container: {
        display: 'flex',
        flexFlow: 'column nowrap',
    },
});

const TabView = (props: TabViewProps & TabViewDispatch & WithStyles<typeof styles>) => (
    <Paper className={classNames(props.classes.container, props.className)}>
        {props.tabs &&
            <AppBar position="static" color="default">
                <Tabs
                    value={props.activeTab}
                    onChange={(_, val) => props.changeEditorMode(val)}
                    centered={true}
                    indicatorColor="primary"
                >
                    {props.tabs.map(cfg => {
                        const { inner, ...rest } = cfg;
                        return <Tab key={cfg.value} {...rest}/>;
                    })}
                </Tabs>
            </AppBar>
        }
        {props.inner}
    </Paper>
);

export default withStyles(styles)(TabView);
