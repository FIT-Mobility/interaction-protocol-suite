import { Action } from '../../actions';
import {
    CLOSE_FILE,
    OPEN_FILE_FAIL,
    OPEN_FILE_FINISH,
    OPEN_FILE_START,
} from '../../actions/components/editor/existing-xsds-file-viewer';
import { ExistingFilesEditorComponentState } from '../../state';

export default (
    state: ExistingFilesEditorComponentState = {
        fileLoadError: null,
        isLoadingFile: false,
        singleFileContents: null,
    },
    ac: Action,
): ExistingFilesEditorComponentState => {
    switch (ac.type) {
        case CLOSE_FILE:
            return {
                ...state,
                fileLoadError: null,
                isLoadingFile: false,
                singleFileContents: null,
            };
        case OPEN_FILE_FAIL:
            return {
                ...state,
                fileLoadError: ac.payload,
                isLoadingFile: false,
            };
        case OPEN_FILE_FINISH:
            return {
                ...state,
                isLoadingFile: false,
                singleFileContents: ac.payload,
            };
        case OPEN_FILE_START:
            return {
                ...state,
                fileLoadError: null,
                isLoadingFile: true,
            };
        default:
            return state;
    }
};
