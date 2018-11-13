import IconButton from '@material-ui/core/es/IconButton';
import Input from '@material-ui/core/es/Input';
import InputLabel from '@material-ui/core/es/InputLabel';
import List from '@material-ui/core/es/List';
import ListItem from '@material-ui/core/es/ListItem';
import ListItemSecondaryAction from '@material-ui/core/es/ListItemSecondaryAction';
import ListSubheader from '@material-ui/core/es/ListSubheader';
import MenuItem from '@material-ui/core/es/MenuItem';
import Select from '@material-ui/core/es/Select';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import TextField from '@material-ui/core/es/TextField';
import Add from '@material-ui/icons/Add';
import Delete from '@material-ui/icons/Delete';
import { Element, Function as Fn } from 'omp-schema';
import * as React from 'react';

import { row } from '../utils/flex-primitives';
import { FocusableTextField } from "../utils/textfield";

import { safe } from '.';

export interface FunctionsEditorProps {
    currentItem: Fn;
    elements: Element[];
    disableEditing: boolean;
}
export interface FunctionsEditorDispatch {
    addAssertion: (fnId: string) => void;
    changeAssertion: (fnId: string, idx: number, newValue: string) => void;
    changeRequestType: (id: string, type: string) => void;
    changeResponseType: (id: string, type: string) => void;
    nameChanged: (id: string, name: string) => void;
    removeAssertion: (fnId: string, idx: number) => void;
}

const styles = theme => createStyles({
    assertionInput: {
        margin: `${theme.spacing.unit * 2}px 0`,
        paddingRight: theme.spacing.unit * 8,
    },
    assertionItem: {
        padding: 0,
    },
    monospace: {
        fontFamily: 'monospace',
    },
    row,
    selectType: {
        marginBottom: theme.spacing.unit * 4,
    },
});

/* tslint:disable:max-line-length */
const FunctionsEditor = (props: FunctionsEditorProps & FunctionsEditorDispatch & WithStyles<typeof styles>) => (
    <>
        <FocusableTextField
            label="Function Name"
            disabled={props.disableEditing}
            fullWidth={true}
            onChange={(ev: any) => props.nameChanged(safe(props.currentItem, it => it.id), ev.target.value)}
            value={safe(props.currentItem, it => it.name)}
            focusId='functionname'
        />

        <InputLabel htmlFor="fn-req-type">Request Element</InputLabel>
        <Select
            className={props.classes.selectType}
            disabled={props.disableEditing}
            fullWidth={true}
            input={<Input id="fn-req-type"/>}
            onChange={(ev: any) => props.changeRequestType(safe(props.currentItem, it => it.id), ev.target.value)}
            value={safe(props.currentItem, it => it.request)}
        >
            <MenuItem value="">Top level elements from XSD editor:</MenuItem>
            {props.elements!.map(({ id, name, type }) => (
                <MenuItem
                    key={id}
                    value={id}
                >
                    {name} ({type})
                </MenuItem>
            ))}
        </Select>

        <InputLabel htmlFor="fn-resp-type">Response Element</InputLabel>
        <Select
            className={props.classes.selectType}
            disabled={props.disableEditing}
            fullWidth={true}
            input={<Input id="fn-resp-type"/>}
            onChange={(ev: any) => props.changeResponseType(safe(props.currentItem, it => it.id), ev.target.value)}
            value={safe(props.currentItem, it => it.response)}
        >
            <MenuItem value="">None</MenuItem>
            {props.elements!.map(({ id, name, type }) => (
                <MenuItem
                    key={id}
                    value={id}
                >
                    {name} ({type})
                </MenuItem>
            ))}
        </Select>

        <List>
            <ListSubheader className={props.classes.assertionItem}>
                XSD 1.1 Assertions

                <ListItemSecondaryAction>
                    <IconButton
                        title="Add"
                        disabled={props.disableEditing}
                        onClick={() => props.addAssertion(safe(props.currentItem, it => it.id))}
                    >
                        <Add/>
                    </IconButton>
                </ListItemSecondaryAction>
            </ListSubheader>

            {props.currentItem && props.currentItem.assertions.map((assertion, idx) => (
                <ListItem key={idx} className={props.classes.assertionItem}>
                    <TextField
                        className={props.classes.assertionInput}
                        disabled={props.disableEditing}
                        inputProps={{ className: props.classes.monospace }}
                        fullWidth={true}
                        value={assertion}
                        onChange={ev => props.changeAssertion(safe(props.currentItem, it => it.id), idx, ev.target.value)}
                    />
                    <ListItemSecondaryAction>
                        <IconButton
                            title="Remove"
                            disabled={props.disableEditing}
                            onClick={() => props.removeAssertion(safe(props.currentItem, it => it.id), idx)}
                        >
                            <Delete/>
                        </IconButton>
                    </ListItemSecondaryAction>
                </ListItem>
            ))}
        </List>
    </>
);
/* tslint:enable */

export default withStyles(styles)(FunctionsEditor as any) as any;
