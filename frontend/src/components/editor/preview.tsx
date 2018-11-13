import AppBar from '@material-ui/core/es/AppBar/AppBar';
import CircularProgress from '@material-ui/core/es/CircularProgress';
import Subheader from '@material-ui/core/es/ListSubheader';
import Paper from '@material-ui/core/es/Paper';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import Tab from '@material-ui/core/es/Tab';
import Tabs from '@material-ui/core/es/Tabs';
import ChangeHistory from '@material-ui/icons/ChangeHistory';
import Error from '@material-ui/icons/Error';
import PageView from '@material-ui/icons/Pageview';
import * as React from 'react';

import { HttpError, PreviewMode } from '../../state';

export interface PreviewProps {
    className?: string;
    previewLink: string;
    previewLoadError: HttpError | null;
    previewLoading: boolean;
    previewMode: PreviewMode;
}
export interface PreviewDispatch {
    changeMode: (newMode: PreviewMode) => void;
}

const styles = theme => createStyles({
    embed: {
        height: '100%',
        width: '100%',
    },
    progress: {
        color: 'black',
        margin: `${theme.spacing.unit / 2}px`,
    },
});

type Props = PreviewProps &
    PreviewDispatch &
    WithStyles<typeof styles>;

const PreviewIconView = (props: Props) => {
    if (props.previewLoading) {
        return <CircularProgress className={props.classes.progress} size={16}/>;
    } else if (props.previewLoadError) {
        return <Error/>;
    } else {
        return <PageView/>;
    }
};
const PreviewIcon = withStyles(styles)(PreviewIconView);

const PreviewBlock = (props: Props & { className?: string }) => (
    <Paper className={props.className}>
        <AppBar
            position="static"
            color="default"
        >
            <Tabs
                value={props.previewMode}
                onChange={(_, val) => props.changeMode(val)}
                centered={true}
                indicatorColor="primary"
            >
                <Tab
                    label="Preview"
                    icon={<PreviewIcon {...props}/>}
                    value={PreviewMode.Preview}
                    selected={props.previewMode === PreviewMode.Preview}
                    title={(props.previewLoadError && props.previewLoadError.message)!}
                />
                <Tab
                    label="Changes"
                    icon={<ChangeHistory/>}
                    value={PreviewMode.Changes}
                    selected={props.previewMode === PreviewMode.Changes}
                />
            </Tabs>
        </AppBar>

        {props.previewMode === PreviewMode.Preview
            ?   props.previewLoadError
                ?   (
                        <Subheader>{props.previewLoadError.message!}</Subheader>
                    )
                :   (
                        <embed
                            type="application/pdf"
                            src={props.previewLink}
                            className={props.classes.embed}
                        />
                    )
            : <Subheader>To be implemented.</Subheader>}
    </Paper>
);

export default withStyles(styles)(PreviewBlock);
