import Button from '@material-ui/core/es/Button';
import Dialog from '@material-ui/core/es/Dialog';
import DialogActions from '@material-ui/core/es/DialogActions';
import DialogContent from '@material-ui/core/es/DialogContent';
import DialogContentText from '@material-ui/core/es/DialogContentText';
import DialogTitle from '@material-ui/core/es/DialogTitle';
import * as React from 'react';

export interface LiveRevisionChangeNotifierProps {
    projectUrlSlugWithChangedLiveRevision: string;
}

export interface LiveRevisionChangeNotifierDispatch {
    discardNotification: () => void;
}

type Props = LiveRevisionChangeNotifierProps &
    LiveRevisionChangeNotifierDispatch;

const LiveRevisionChangeNotifier = (props: Props) => (
    <Dialog open={!!props.projectUrlSlugWithChangedLiveRevision} onClose={props.discardNotification}>
        <DialogTitle>New live revision is available</DialogTitle>
        <DialogContent>
            <DialogContentText>
                A new live revision was activated for this project.
            </DialogContentText>
        </DialogContent>
        <DialogActions>
            <Button onClick={props.discardNotification}>
                Ignore
            </Button>
            <Button href={`/projects/${props.projectUrlSlugWithChangedLiveRevision}/live`} color="primary">
                Switch to new live revision
            </Button>
        </DialogActions>
    </Dialog>
);

export default LiveRevisionChangeNotifier;
