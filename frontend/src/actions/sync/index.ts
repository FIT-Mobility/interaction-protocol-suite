import {
    ArrayDelta,
    Comment,
    DataType,
    DataTypeDocumentation,
    Element,
    Function as Fn,
    Project, ProjectRevision,
    Sequence,
    Service,
    XsdOperation,
} from '@ips/shared-js';
import { SchemaAnalysis } from '@ips/shared-js/xsd-analysis';

import { PayloadAction, Types } from '..';
import { RemoveAction, Update } from '..';
import { ConnectionState } from '../../state';

// Chooses appropriate implementation for sync layer
export * from './yjs';

export type SyncActions =
    | AddXsdDeltasAction
    | RemoveXsdDeltasAction
    | AddCommentAction
    | RemoveCommentAction
    | UpdateCommentAction
    | AddDataTypeAction
    | RemoveDataTypeAction
    | UpdateDataTypeAction
    | AddDataTypeDocumentationAction
    | RemoveDataTypeDocumentationAction
    | UpdateDataTypeDocumentationAction
    | AddElementAction
    | RemoveElementAction
    | UpdateElementAction
    | AddFunctionAction
    | RemoveFunctionAction
    | UpdateFunctionAction
    | AddSequenceAction
    | RemoveSequenceAction
    | UpdateSequenceAction
    | AddServiceAction
    | RemoveServiceAction
    | UpdateServiceAction
    | AddProjectAction
    | RemoveProjectAction
    | UpdateProjectAction
    | AddProjectRevisionAction
    | RemoveProjectRevisionAction
    | UpdateProjectRevisionAction
    | InitSyncFailAction
    | InitSyncFinishAction
    | InitSyncStartAction
    | ChangeConnectionStateAction
    | UpdateSchemaAnalysisAction
    | OpenProjectFailAction
    | OpenProjectFinishAction
    | OpenProjectStartAction
    | CloseProjectFailAction
    | CloseProjectFinishAction
    | CloseProjectStartAction
    | NotifyLiveRevisionChangedAction;

export interface AddXsdDeltasAction extends PayloadAction<ArrayDelta<XsdOperation>> {
    type: Types.AddXsdDeltas;
}
export interface RemoveXsdDeltasAction extends PayloadAction<ArrayDelta<XsdOperation>> {
    type: Types.RemoveXsdDeltas;
}

export interface AddCommentAction extends PayloadAction<Comment> {
    type: Types.AddComment;
}
export interface RemoveCommentAction extends RemoveAction {
    type: Types.RemoveComment;
}
export interface UpdateCommentAction extends PayloadAction<Comment> {
    type: Types.UpdateComment;
}

export interface AddDataTypeAction extends PayloadAction<DataType> {
    type: Types.AddDataType;
}
export interface RemoveDataTypeAction extends RemoveAction {
    type: Types.RemoveDataType;
}
export interface UpdateDataTypeAction extends PayloadAction<Update<DataType>> {
    type: Types.UpdateDataType;
}
export interface AddDataTypeDocumentationAction extends PayloadAction<DataTypeDocumentation> {
    type: Types.AddDataTypeDocumentation;
}
export interface RemoveDataTypeDocumentationAction extends RemoveAction {
    type: Types.RemoveDataTypeDocumentation;
}
export interface UpdateDataTypeDocumentationAction extends PayloadAction<Update<DataTypeDocumentation>> {
    type: Types.UpdateDataTypeDocumentation;
}

export interface AddElementAction extends PayloadAction<Element> {
    type: Types.AddElement;
}
export interface RemoveElementAction extends RemoveAction {
    type: Types.RemoveElement;
}
export interface UpdateElementAction extends PayloadAction<Update<Element>> {
    type: Types.UpdateElement;
}

export interface AddFunctionAction extends PayloadAction<Fn> {
    type: Types.AddFunction;
}
export interface RemoveFunctionAction extends RemoveAction {
    type: Types.RemoveFunction;
}
export interface UpdateFunctionAction extends PayloadAction<Update<Fn>> {
    type: Types.UpdateFunction;
}

export interface AddSequenceAction extends PayloadAction<Sequence> {
    type: Types.AddSequence;
}
export interface RemoveSequenceAction extends RemoveAction {
    type: Types.RemoveSequence;
}
export interface UpdateSequenceAction extends PayloadAction<Update<Sequence>> {
    type: Types.UpdateSequence;
}

export interface AddServiceAction extends PayloadAction<Service> {
    type: Types.AddService;
}
export interface RemoveServiceAction extends RemoveAction {
    type: Types.RemoveService;
}
export interface UpdateServiceAction extends PayloadAction<Update<Service>> {
    type: Types.UpdateService;
}

export interface AddProjectAction extends PayloadAction<Project> {
    type: Types.AddProject;
}
export interface RemoveProjectAction extends RemoveAction {
    type: Types.RemoveProject;
}
export interface UpdateProjectAction extends PayloadAction<Project> {
    type: Types.UpdateProject;
}

export interface AddProjectRevisionAction extends PayloadAction<ProjectRevision> {
    type: Types.AddProjectRevision;
}
export interface RemoveProjectRevisionAction extends RemoveAction {
    type: Types.RemoveProjectRevision;
}
export interface UpdateProjectRevisionAction extends PayloadAction<ProjectRevision> {
    type: Types.UpdateProjectRevision;
}

export interface OpenProjectStartAction extends PayloadAction<string> {
    type: Types.OpenProject_Start;
}
export interface OpenProjectFailAction extends PayloadAction<Error> {
    type: Types.OpenProject_Fail;
    error: true;
}
export interface OpenProjectFinishAction extends PayloadAction<string> {
    type: Types.OpenProject_Finish;
}

export interface CloseProjectStartAction {
    type: Types.CloseProject_Start;
}
export interface CloseProjectFailAction extends PayloadAction<Error> {
    type: Types.CloseProject_Fail;
    error: true;
}
export interface CloseProjectFinishAction {
    type: Types.CloseProject_Finish;
}

export interface InitSyncStartAction {
    type: Types.InitSync_Start;
}
export interface InitSyncFailAction extends PayloadAction<Error> {
    type: Types.InitSync_Fail;
    error: true;
}
export interface InitSyncFinishAction {
    type: Types.InitSync_Finish;
}

export interface ChangeConnectionStateAction extends PayloadAction<ConnectionState> {
    type: Types.ChangeConnectionState;
}

export interface UpdateSchemaAnalysisAction extends PayloadAction<SchemaAnalysis> {
    type: Types.UpdateSchemaAnalysis;
}

export function addXsdDeltas(delta: ArrayDelta<XsdOperation>): AddXsdDeltasAction {
    return {
        type: Types.AddXsdDeltas,
        payload: delta,
    };
}

export function addDataType(payload: DataType): AddDataTypeAction {
    return {
        type: Types.AddDataType,
        payload,
    };
}

export function removeDataType(id: string): RemoveDataTypeAction {
    return {
        type: Types.RemoveDataType,
        payload: id,
    };
}

export function updateDataType(payload: Update<DataType>): UpdateDataTypeAction {
    return {
        type: Types.UpdateDataType,
        payload,
    };
}

export function addElement(payload: Element): AddElementAction {
    return {
        type: Types.AddElement,
        payload,
    };
}

export function removeElement(id: string): RemoveElementAction {
    return {
        type: Types.RemoveElement,
        payload: id,
    };
}

export function updateElement(payload: Update<Element>): UpdateElementAction {
    return {
        type: Types.UpdateElement,
        payload,
    };
}

export function updateSchemaAnalysis(an: SchemaAnalysis): UpdateSchemaAnalysisAction {
    return {
        type: Types.UpdateSchemaAnalysis,
        payload: an,
    };
}

export interface NotifyLiveRevisionChangedAction extends PayloadAction<string> {
    type: Types.NotifyLiveRevisionChanged;
}
