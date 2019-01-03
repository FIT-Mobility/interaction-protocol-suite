import { DomainItemType } from '@ips/shared-js';

import { Action, Types } from '../../actions';
import { EditorComponentState } from '../../state';

function createDefault(): EditorComponentState {
    return {
        existingXsdsFileUploadError: null,
        existingXsdsFileUploadInProgress: false,
        isDomainMenuCollapsed: false,
        isDownloadMenuOpen: false,
        isExistingXsdsMenuOpen: false,
        isPreviewCollapsed: false,
        isUploadMenuOpen: false,
        openMenus: [],
        xsdsFileToUpload: null,
    };
}

function actionToDomainItemType(actionType: Types): DomainItemType {
    switch (actionType) {
        case Types.AddDataType:
            return DomainItemType.DataType;
        case Types.AddFunction:
            return DomainItemType.Function;
        case Types.AddSequence:
            return DomainItemType.Sequence;
        case Types.AddService:
            return DomainItemType.Service;
        default:
            throw new Error("Unknown action type.");
    }
}

export default function(
    state: EditorComponentState = createDefault(),
    action: Action,
): EditorComponentState {
    switch (action.type) {
        case Types.AddDataType:
        case Types.AddFunction:
        case Types.AddSequence:
        case Types.AddService:
            const ty = actionToDomainItemType(action.type);
            return (state.openMenus.indexOf(ty) === -1) // Doesn't include the item
                ? { ...state, openMenus: [...state.openMenus, ty] }
                : state;
        case Types.CloseProject_Start:
            return createDefault();
        case Types.OpenDownloadMenu:
            return {
                ...state,
                isDownloadMenuOpen: true,
            };
        case Types.CloseDownloadMenu:
            return {
                ...state,
                isDownloadMenuOpen: false,
            };
        case Types.OpenExistingXsdsMenu:
            return {
                ...state,
                isExistingXsdsMenuOpen: true,
            };
        case Types.CloseExistingXsdsMenu:
            return {
                ...state,
                isExistingXsdsMenuOpen: false,
            };
        case Types.ChangeXsdsFileToUpload:
            return {
                ...state,
                xsdsFileToUpload: action.payload,
            };
        case Types.OpenXsdsUploadMenu:
            return {
                ...state,
                isUploadMenuOpen: true,
            };
        case Types.UploadExistingXsdsFileStart:
            return {
                ...state,
                existingXsdsFileUploadError: null,
                existingXsdsFileUploadInProgress: true,
            };
        case Types.CloseXsdsUploadMenu:
        case Types.UploadExistingXsdsFileSuccess:
            return {
                ...state,
                existingXsdsFileUploadError: null,
                existingXsdsFileUploadInProgress: false,
                isUploadMenuOpen: false,
            };
        case Types.UploadExistingXsdsFileFail:
            return {
                ...state,
                existingXsdsFileUploadError: action.payload,
                existingXsdsFileUploadInProgress: false,
            };
        case Types.SetDomainItemMenuCollapsation:
            return {
                ...state,
                isDomainMenuCollapsed: action.payload,
            };
        case Types.ToggleDomainItemMenu:
            return {
                ...state,
                openMenus: (state.openMenus.indexOf(action.payload) !== -1) // includes the item
                    ? state.openMenus.filter(menu => menu !== action.payload)
                    : [...state.openMenus, action.payload],
            };
        case Types.SetPreviewCollapsation:
            return {
                ...state,
                isPreviewCollapsed: action.payload,
            };
        default:
            return state;
    }
}
