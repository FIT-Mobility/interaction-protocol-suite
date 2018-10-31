import Paper from '@material-ui/core/es/Paper';
import { createStyles } from '@material-ui/core/es/styles';

import { attachStyles } from './styles-attached';

const styles = theme => createStyles({
    root: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        marginBottom: theme.spacing.unit * 2,
        maxWidth: '700px',
        width: '100%',
        overflowY: 'auto',
    },
});

export default attachStyles(Paper, styles);
