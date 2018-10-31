import { applyMiddleware, combineReducers, compose, createStore, Store } from 'redux';
import { initializeCurrentLocation, routerForBrowser } from 'redux-little-router';
import thunkMiddleware from 'redux-thunk';

import { pushInProject } from "./actions";
import { loadCollapsationStates } from './actions/components/editor';
import { loadAutoGeneratePreview } from './actions/components/editor/preview';
import { assertIndexedDb, tryRelogin } from './actions/init';
import middlewares from './middleware';
import reducers from './reducers';
import { State } from './state';

/* tslint:disable:object-literal-sort-keys */
const router = routerForBrowser({
    routes: {
        '/': {},
        '/register': {
            isRegistering: true,
        },
        '/projects/:projectUrlSlug/revisions': {},
        '/projects/:projectUrlSlug/:revisionUrlSlug': {
            '/': {},
            '/data-types/:datatypeId': {},
            '/existing-files': {
                '/': {},
                '/:fileIndex': {},
            },
            '/functions/:functionId': {},
            '/sequences/:sequenceId': {},
            '/services/:serviceId': {},
            '/project': {},
            '/xsd': {},
        },
    },
});
/* tslint:enable */

/* Attach to redux dev tools (Chrome extension) */
const w = window as any;
const composeEnhancers = (process.env.NODE_ENV !== 'production' &&
                          w.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__)
    ? w.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__
    : compose;

export const store = createStore(
    combineReducers({
        ...reducers,
        router: router.reducer,
    }),
    composeEnhancers(
        router.enhancer,
        applyMiddleware(
            thunkMiddleware,
            router.middleware,
            ...middlewares,
        ),
    ),
);

// Dispatch init actions like router reconfiguration and automatic relogin

const initialLocation = store.getState().router;
if (initialLocation) {
    store.dispatch(initializeCurrentLocation(initialLocation));
    pushInProject(store.getState().router.pathname);
}

const anyStore = store as Store<State, any>;

anyStore.dispatch(tryRelogin());
anyStore.dispatch(loadAutoGeneratePreview());
anyStore.dispatch(loadCollapsationStates());
anyStore.dispatch(assertIndexedDb());
