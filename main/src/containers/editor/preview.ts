import { connect } from 'react-redux';

import { changePreviewMode } from '../../actions/components/editor/preview';
import PreviewView, { PreviewDispatch, PreviewProps } from '../../components/editor/preview';
import { PreviewMode, State } from '../../state';

const mapStateToProps = (state: State, op): PreviewProps => ({
    className: op.className,
    previewLink: state.components.preview.previewLinks[0],
    previewLoadError: state.components.preview.previewLoadError,
    previewLoading: state.components.preview.previewLoading,
    /* tslint:disable-next-line:triple-equals */
    previewMode: state.router.query.preview == (PreviewMode.Changes + '')
        ? PreviewMode.Changes
        : PreviewMode.Preview,
});
const mapDispatchToProps: PreviewDispatch = {
    changeMode: changePreviewMode,
};

const Preview = connect(
    mapStateToProps,
    mapDispatchToProps,
)(PreviewView);

export default Preview;
