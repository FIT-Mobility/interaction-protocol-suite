import { connect } from 'react-redux';

import { discardNotification, } from '../actions/components/live-revision-change-notifier';
import LiveRevisionChangeNotifierView, {
    LiveRevisionChangeNotifierDispatch,
    LiveRevisionChangeNotifierProps
} from '../components/live-revision-change-notifier';
import { State } from '../state';

const mapStateToProps = (state: State): LiveRevisionChangeNotifierProps => ({
    projectUrlSlugWithChangedLiveRevision:
        state.components.liveRevisionChangeNotifier.projectUrlSlugWithChangedLiveRevision,
});
const mapDispatchToProps: LiveRevisionChangeNotifierDispatch = {
    discardNotification,
};

const LiveRevisionChangeNotifier = connect(
    mapStateToProps,
    mapDispatchToProps,
)(LiveRevisionChangeNotifierView);

export default LiveRevisionChangeNotifier;
