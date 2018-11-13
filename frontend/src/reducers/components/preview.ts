import { Action, Types } from '../../actions';
import { PreviewComponentState } from '../../state';

export default function(
    state: PreviewComponentState = {
        autoGenerate: true,
        previewLinks: ['', ''],
        previewLoadError: null,
        previewLoading: false,
    },
    action: Action,
): PreviewComponentState {
    switch (action.type) {
        case Types.CloseProject_Start:
            return {
                ...state,
                previewLinks: ['', ''],
                previewLoadError: null,
                previewLoading: true,
            };
        case Types.LoadPreview_Start:
            return {
                ...state,
                previewLoadError: null,
                previewLoading: true,
            };
        case Types.LoadPreview_Fail:
            return {
                ...state,
                previewLoadError: action.payload,
                previewLoading: false,
            };
        case Types.LoadPreview_Finish:
            return {
                ...state,
                previewLinks: action.payload,
                previewLoadError: null,
                previewLoading: false,
            };
        case Types.SetAutoGeneratePreview: {
            return {
                ...state,
                autoGenerate: action.payload,
            };
        }
        default:
            return state;
    }
}
