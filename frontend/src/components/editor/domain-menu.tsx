import Collapse from '@material-ui/core/es/Collapse';
import IconButton from '@material-ui/core/es/IconButton';
import List from '@material-ui/core/es/List';
import ListItem from '@material-ui/core/es/ListItem';
import ListItemIcon from '@material-ui/core/es/ListItemIcon';
import ListItemSecondaryAction from '@material-ui/core/es/ListItemSecondaryAction';
import Paper from '@material-ui/core/es/Paper';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import Add from '@material-ui/icons/Add';
import CompareArrows from '@material-ui/icons/CompareArrows';
import Dashboard from '@material-ui/icons/Dashboard';
import Delete from '@material-ui/icons/Delete';
import Description from '@material-ui/icons/Description';
import Polymer from '@material-ui/icons/Polymer';
import Timeline from '@material-ui/icons/Timeline';
import ViewCarousel from '@material-ui/icons/ViewCarousel';
import { DataType, DomainItemType, Function as Fn, Sequence, Service } from 'omp-schema';
import * as React from 'react';

import { hidden } from '../utils/flex-primitives';
import SingleLineText from '../utils/single-line-listitem';

export interface DomainMenuProps {
    dataTypesMenuOpen: boolean;
    functionsMenuOpen: boolean;
    sequencesMenuOpen: boolean;
    servicesMenuOpen: boolean;
    currentItemId: string | null;

    dataTypes: DataType[];
    functions: Fn[];
    sequences: Sequence[];
    services: Service[];
    disableEditing: boolean;
}

export interface DomainMenuDispatch {
    createItem: (ty: DomainItemType) => void;
    deleteItem: (ty: DomainItemType, id: string) => void;
    openItem: (ty: DomainItemType, id: string) => void;
    openProjectDocs: () => void;
    openXsdEditor: () => void;
    toggleMenu: (menu: DomainItemType) => void;
}

const styles = theme => createStyles({
    hidden,
    listItem: {
        backgroundColor: '#f5f5f5',
        padding: `${theme.spacing.unit * 2}px ${theme.spacing.unit * 4}px`,
    },
    listItemFocused: {
        fontWeight: [['bold'], '!important'] as any,
    },
});

type Props = DomainMenuProps &
    DomainMenuDispatch &
    { className?: string } &
    WithStyles<typeof styles>;

const DomainMenu: React.SFC<Props> = (props) => (
    <Paper className={props.className} role="nav">
        <List>
            <ListItem
                button={true}
                onClick={props.openProjectDocs}
            >
                <ListItemIcon>
                    <ViewCarousel/>
                </ListItemIcon>
                <SingleLineText primary="Project"/>
            </ListItem>

            <ListItem
                button={true}
                onClick={() => props.toggleMenu(DomainItemType.Service)}
            >
                <ListItemIcon>
                    <Dashboard/>
                </ListItemIcon>
                <SingleLineText primary="Services"/>
                <ListItemSecondaryAction className={props.disableEditing ? props.classes.hidden : ""}>
                    <IconButton
                        onClick={() => props.createItem(DomainItemType.Service)}
                        title="Add Service"
                    >
                        <Add/>
                    </IconButton>
                </ListItemSecondaryAction>
            </ListItem>
            <Collapse
                in={props.servicesMenuOpen}
                timeout="auto"
                unmountOnExit={true}
            >
                {props.services.map(({id, name}) => (
                    <ListItem
                        key={id}
                        className={props.classes.listItem}
                        button={true}
                        onClick={() => props.openItem(DomainItemType.Service, id)}
                    >
                        <SingleLineText
                            primary={name || 'Service'}
                            classes={{
                                primary: (props.currentItemId === id && props.classes.listItemFocused) || undefined
                            }}
                        />
                        <ListItemSecondaryAction className={props.disableEditing ? props.classes.hidden : ""}>
                            <IconButton
                                onClick={() => props.deleteItem(DomainItemType.Service, id)}
                                title={`Remove ${name}`}
                            >
                                <Delete/>
                            </IconButton>
                        </ListItemSecondaryAction>
                    </ListItem>
                ))}
            </Collapse>

            <ListItem
                button={true}
                onClick={() => props.toggleMenu(DomainItemType.Sequence)}
            >
                <ListItemIcon>
                    <Timeline/>
                </ListItemIcon>
                <SingleLineText primary="Sequences"/>
                <ListItemSecondaryAction className={props.disableEditing ? props.classes.hidden : ""}>
                    <IconButton
                        onClick={() => props.createItem(DomainItemType.Sequence)}
                        title="Add Sequence"
                    >
                        <Add/>
                    </IconButton>
                </ListItemSecondaryAction>
            </ListItem>
            <Collapse
                in={props.sequencesMenuOpen}
                timeout="auto"
                unmountOnExit={true}
            >
                {props.sequences.map(({id, name}) => (
                    <ListItem
                        key={id}
                        className={props.classes.listItem}
                        button={true}
                        onClick={() => props.openItem(DomainItemType.Sequence, id)}
                    >
                        <SingleLineText
                            primary={name || 'Sequence'}
                            classes={{
                                primary: (props.currentItemId === id && props.classes.listItemFocused) || undefined
                            }}
                        />
                        <ListItemSecondaryAction className={props.disableEditing ? props.classes.hidden : ""}>
                            <IconButton
                                onClick={() => props.deleteItem(DomainItemType.Sequence, id)}
                                title={`Remove ${name}`}
                            >
                                <Delete/>
                            </IconButton>
                        </ListItemSecondaryAction>
                    </ListItem>
                ))}
            </Collapse>

            <ListItem
                button={true}
                onClick={() => props.toggleMenu(DomainItemType.Function)}
            >
                <ListItemIcon>
                    <CompareArrows/>
                </ListItemIcon>
                <SingleLineText primary="Functions"/>
                <ListItemSecondaryAction className={props.disableEditing ? props.classes.hidden : ""}>
                    <IconButton
                        onClick={() => props.createItem(DomainItemType.Function)}
                        title="Add Function"
                    >
                        <Add/>
                    </IconButton>
                </ListItemSecondaryAction>
            </ListItem>
            <Collapse
                in={props.functionsMenuOpen}
                timeout="auto"
                unmountOnExit={true}
            >
                {props.functions.map(({id, name}) => (
                    <ListItem
                        key={id}
                        className={props.classes.listItem}
                        button={true}
                        onClick={() => props.openItem(DomainItemType.Function, id)}
                    >
                        <SingleLineText
                            primary={name || 'Function'}
                            classes={{
                                primary: (props.currentItemId === id && props.classes.listItemFocused) || undefined
                            }}
                        />
                        <ListItemSecondaryAction className={props.disableEditing ? props.classes.hidden : ""}>
                            <IconButton
                                onClick={() => props.deleteItem(DomainItemType.Function, id)}
                                title={`Remove ${name}`}
                            >
                                <Delete/>
                            </IconButton>
                        </ListItemSecondaryAction>
                    </ListItem>
                ))}
            </Collapse>

            <ListItem
                button={true}
                onClick={() => props.toggleMenu(DomainItemType.DataType)}
            >
                <ListItemIcon>
                    <Polymer/>
                </ListItemIcon>
                <SingleLineText primary="Data Types"/>
            </ListItem>
            <Collapse
                in={props.dataTypesMenuOpen}
                timeout="auto"
                unmountOnExit={true}
            >
                {props.dataTypes.map(({id, name}) => (
                    <ListItem
                        key={id}
                        className={props.classes.listItem}
                        button={true}
                        onClick={() => props.openItem(DomainItemType.DataType, id)}
                    >
                        <SingleLineText
                            primary={name || 'Data Type'}
                            classes={{
                                primary: (props.currentItemId === id && props.classes.listItemFocused) || undefined
                            }}
                        />
                    </ListItem>
                ))}
            </Collapse>

            <ListItem button={true} onClick={props.openXsdEditor}>
                <ListItemIcon>
                    <Description/>
                </ListItemIcon>
                <SingleLineText primary="XSD editor"/>
            </ListItem>
        </List>
    </Paper>
);

export default withStyles(styles)(DomainMenu);
