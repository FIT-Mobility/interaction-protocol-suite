import Snackbar from '@material-ui/core/Snackbar';
import * as React from 'react';
import { connect } from "react-redux";

import { displayError, hideError } from "../../actions/components/error-snackbar";
import { State } from "../../state";
import { store } from "../../store";

export interface ErrorSnackbarProps {
    isOpen: boolean;
    message: string | null;
}

interface ErrorSnackbarDispatch {
    displayError: (message: string | null) => void;
    hideError: () => void;
}

const mapStateToProps = (state: State): ErrorSnackbarProps => ({
    isOpen: state.components.errorSnackbar.isOpen,
    message: state.components.errorSnackbar.message,
});

const mapDispatchToProps: ErrorSnackbarDispatch = {
    displayError,
    hideError,
};

type Props = ErrorSnackbarProps & ErrorSnackbarDispatch;

const ErrorSnackbar = (props: Props) => (
    <Snackbar
        anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'center',
        }}
        open={props.isOpen}
        autoHideDuration={6000}
        message={props.message!}
        onClose={() => {
            store.dispatch(hideError());
        }}
    />
);

export default connect(mapStateToProps, mapDispatchToProps)(ErrorSnackbar);
