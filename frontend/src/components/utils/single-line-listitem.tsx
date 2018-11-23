import ListItemText from '@material-ui/core/es/ListItemText';
import { createStyles } from '@material-ui/core/es/styles';

import { attachStyles } from './styles-attached';

const styles = createStyles({
    primary: {
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
    },
    root: {
        overflow: 'hidden',
    },
});

export default attachStyles(ListItemText, styles);
