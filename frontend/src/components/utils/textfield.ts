import { createStyles } from '@material-ui/core/es/styles';
import TextField, { TextFieldProps } from "@material-ui/core/es/TextField";

import { attachStyles } from './styles-attached';
import withFocus from "./with-focus";

const styles = theme => createStyles({
    root: {
        marginBottom: theme.spacing.unit * 2,
    },
});

export const TextfieldWMargin = attachStyles(TextField, styles);
export const FocusableTextField = withFocus(TextfieldWMargin as React.ComponentClass<TextFieldProps>);
