import Card from '@material-ui/core/es/Card';
import { createStyles } from '@material-ui/core/es/styles';

import { attachStyles } from './styles-attached';

const styles = createStyles({
    root: {
        width: '100%',
    },
});

export default attachStyles(Card, styles);
