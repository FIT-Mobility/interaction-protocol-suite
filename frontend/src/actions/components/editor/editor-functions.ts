import { ThunkAction } from 'redux-thunk';

import { Action } from '../..';
import { State } from '../../../state';
import {
    updateFunctionAssertions,
    updateFunctionName,
    updateFunctionRequestType,
    updateFunctionResponseType,
} from '../../sync';

export {
    updateFunctionName as nameChanged,
    updateFunctionRequestType as requestTypeChanged,
    updateFunctionResponseType as responseTypeChanged,
};

export function addAssertion(fnId: string): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        const { projectData } = getState();
        const newAssertions = [...projectData.functions[fnId].assertions, ''];

        dispatch(updateFunctionAssertions(fnId, newAssertions));
    };
}

export function editAssertion(
    fnId: string,
    assertionIdx: number,
    newText: string,
): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        const { projectData } = getState();
        const newAssertions = projectData.functions[fnId].assertions.slice();
        newAssertions[assertionIdx] = newText;

        dispatch(updateFunctionAssertions(fnId, newAssertions));
    };
}

export function removeAssertion(fnId: string, assertionIdx: number): ThunkAction<void, State, void, Action> {
    return (dispatch, getState) => {
        const { projectData } = getState();
        const newAssertions = projectData.functions[fnId].assertions.slice();
        newAssertions.splice(assertionIdx, 1);

        dispatch(updateFunctionAssertions(fnId, newAssertions));
    };
}
