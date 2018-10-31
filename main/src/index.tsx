import teal from '@material-ui/core/es/colors/teal';
import { createGenerateClassName, createMuiTheme, jssPreset, MuiThemeProvider } from '@material-ui/core/es/styles';
import createPalette from '@material-ui/core/es/styles/createPalette';
import { create } from 'jss';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import JssProvider from 'react-jss/lib/JssProvider';
import { Provider } from 'react-redux';

const generateClassName = createGenerateClassName();
const jss = create({ ...jssPreset(), insertionPoint: 'jss-insertion-point' });

import { displayError } from "./actions/components/error-snackbar";
import ErrorSnackbar from './components/utils/error-snackbar';
import OmpApp from './containers/omp-app';
import { store } from './store';

const muiTheme = createMuiTheme({
    palette: createPalette({
        primary: teal,
    }),
});

document.addEventListener('DOMContentLoaded', () => {
    const dom = (
        <JssProvider
            jss={jss}
            generateClassName={generateClassName}
        >
            <Provider store={store}>
                <MuiThemeProvider theme={muiTheme}>
                    <OmpApp/>
                    <ErrorSnackbar/>
                </MuiThemeProvider>
            </Provider>
        </JssProvider>
    );
    const el = document.getElementById('react-root');

    ReactDOM.render(dom, el);
});

window.addEventListener('error', (ev: ErrorEvent) => {
    if (!ev) {
        return;
    }
    const message = ev.message;
    const errorState = store.getState().components.errorSnackbar;
    if (!errorState.isOpen || errorState.message !== message) { // listener triggers twice
        store.dispatch(displayError(ev.message));
    }
});
