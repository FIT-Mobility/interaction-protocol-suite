import Checkbox from '@material-ui/core/es/Checkbox';
import List from '@material-ui/core/es/List';
import ListItem from '@material-ui/core/es/ListItem';
import Subheader from '@material-ui/core/es/ListSubheader';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import { Function as Fn, Sequence, Service } from 'omp-schema';
import * as React from 'react';

import SingleLineText from '../utils/single-line-listitem';
import { FocusableTextField } from "../utils/textfield";

import { safe } from '.';

export interface ServicesEditorProps {
    currentItem: Service;
    functions: Fn[];
    functionsInService: Record<string, boolean>;
    sequences: Sequence[];
    sequencesInService: Record<string, boolean>;
    disableEditing: boolean;
}
export interface ServicesEditorDispatch {
    nameChanged: (id: string, name: string) => void;
    toggleFunction: (id: string, fnId: string) => void;
    toggleSequence: (id: string, sqId: string) => void;
}

const styles = createStyles({
    body: {
        flex: '0 0 50%',
        overflow: 'hidden',
    },
    split: {
        display: 'flex',
        flexDirection: 'row',
    },
});

// tslint:disable:max-line-length

const ServicesEditor = (props: ServicesEditorProps & ServicesEditorDispatch & WithStyles<typeof styles>) => (
    <>
        <FocusableTextField
            label="Service Name"
            disabled={props.disableEditing}
            fullWidth={true}
            onChange={(ev: any) => props.nameChanged(safe(props.currentItem, it => it.id), ev.target.value)}
            value={safe(props.currentItem, it => it.name)}
            focusId='servicename'
        />

        <div className={props.classes.split}>
            <List className={props.classes.body}>
                <Subheader>Sequences</Subheader>
                {props.sequences!.map(({ id, name }) => (
                    <ListItem key={id}>
                        <Checkbox
                            checked={!!props.sequencesInService[id]}
                            disabled={props.disableEditing}
                            disableRipple={true}
                            onChange={() => props.toggleSequence(safe(props.currentItem, it => it.id), id)}
                        />
                        <SingleLineText primary={name}/>
                    </ListItem>
                ))}
            </List>
            <List className={props.classes.body}>
                <Subheader>Functions</Subheader>
                {props.functions!.map(({ id, name }) => (
                    <ListItem key={id}>
                        <Checkbox
                            checked={!!props.functionsInService[id]}
                            disabled={props.disableEditing}
                            disableRipple={true}
                            onChange={() => props.toggleFunction(safe(props.currentItem, it => it.id), id)}
                        />
                        <SingleLineText primary={name}/>
                    </ListItem>
                ))}
            </List>
        </div>
    </>
);

// tslint:enable

export default withStyles(styles)(ServicesEditor);
