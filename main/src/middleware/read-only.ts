import { Store } from 'redux';

import { Action, Types } from '../actions';
import { State } from '../state';

export default (store: Store<State>) => (next: (ac: any) => any) => (action: Action) => {

    next(action);

    if (!store.getState().components.disableEditing) {
        return;
    }
    switch (action.type) {
        case Types.FocusComponent:

        case Types.OpenProject_Start:
        case Types.OpenProject_Fail:
        case Types.OpenProject_Finish:

        case Types.CloseProject_Start:
        case Types.CloseProject_Fail:
        case Types.CloseProject_Finish:

        case Types.DisplayError:
        case Types.HideError:

        case Types.LoadPreview_Start:
        case Types.LoadPreview_Fail:
        case Types.LoadPreview_Finish:

        case Types.LoadUser:

        // case Types.ClickCommentReference:

        case Types.ChangeLoginUserEmail:
        case Types.ChangeLoginUserPassword:
        case Types.ChangeLoginUserPasswordRepeat:
        case Types.ChangeLoginUserName:
        case Types.SetLoginMessage:
        case Types.Login_Start:
        case Types.Login_Fail:
        case Types.Login_Finish:
        case Types.Logout:
        case Types.NotifyLoginStatusKnown:

        case Types.RestoreRevision_Start:
        case Types.RestoreRevision_Fail:
        case Types.RestoreRevision_Finish:

        case Types.SetAutoGeneratePreview:
        case Types.SetDomainItemMenuCollapsation:
        case Types.SetPreviewCollapsation:
        case Types.ToggleDomainItemMenu:
        case Types.OpenDownloadMenu:
        case Types.CloseDownloadMenu:
        case Types.OpenExistingXsdsMenu:
        case Types.CloseExistingXsdsMenu:

        case Types.InitSync_Start:
        case Types.InitSync_Fail:
        case Types.InitSync_Finish:
        case Types.ChangeConnectionState:

        case Types.NotifyIndexedDbAvailabilityKnown:

        case Types.LoadValidation_Start:
        case Types.LoadValidation_Fail:
        case Types.LoadValidation_Finish:

            return;

        default:
            throw new Error(`read-only mode: ${action.toString()} not on whitelist!`);
    }
};
