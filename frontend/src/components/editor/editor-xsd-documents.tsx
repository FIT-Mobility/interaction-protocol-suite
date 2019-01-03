import { XsdElement } from '@ips/shared-js';
import { AttributeTypes, ElementTypes } from '@ips/shared-js/schema-tree';
import List from '@material-ui/core/es/List';
import ListItem from '@material-ui/core/es/ListItem';
import ListSubheader from '@material-ui/core/es/ListSubheader';
import Paper from '@material-ui/core/es/Paper';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import classNames from 'classnames';
import * as React from 'react';
import { DragDropContext } from 'react-dnd';
import Html5Backend from 'react-dnd-html5-backend';

import XsdElementView from '../../containers/editor/editor-xsd-element-view';
import { hidden } from '../utils/flex-primitives';
import SingleLineText from '../utils/single-line-listitem';

import { xsdDefinitionStyles } from './editor-xsd-element-view';

export interface XsdDocumentsEditorProps {
    className?: string;
    focusedElement: XsdElement | null;
    menuAttributes: AttributeTypes[];
    menuElements: ElementTypes[];
    root: XsdElement | null;
    schema: any;
    disableEditing: boolean;
}
export interface XsdDocumentsEditorDispatch {
    addAttribute: (elementId: string, name: string, position: number) => void;
    addNode: (parentId: string, schemaId: string, position: number) => void;
}

const styles = createStyles({
    container: {
        display: 'flex',
        flexDirection: 'row' as 'row',
        flexGrow: 1,
        overflowY: 'hidden' as 'hidden',
    },
    listHeader: {
        backgroundColor: 'white',
    },
    menu: {
        minWidth: '250px',
    },
    editor: {
        flex: '1 1 70%',
    },
    scroll: {
        maxHeight: '100%',
        overflow: 'auto',
    },
    sidebarItem: {
        backgroundColor: '#f5f5f5',
    },
    hidden,
    xsdDefinitionStyles,
});

/* tslint:disable:max-line-length */

type Props = XsdDocumentsEditorProps &
    XsdDocumentsEditorDispatch &
    WithStyles<typeof styles>;

const XsdDocumentsEditorView = ({
    addAttribute,
    addNode,
    classes,
    className,
    focusedElement,
    menuAttributes,
    menuElements,
    root,
    schema,
    disableEditing,
}: Props) => (
    <Paper className={classNames(classes.container, className)}>
        <List className={classNames(disableEditing ? classes.hidden : classes.menu, classes.scroll)}>
            <ListSubheader className={classes.listHeader}>Add elements</ListSubheader>
            {
                menuElements.map(id => (
                    <ListItem
                        key={id}
                        button={true}
                        className={classes.sidebarItem}
                        onClick={() => addNode(
                            focusedElement!.id,
                            id,
                            (root && Array.isArray(root.content)) ? root.content.length : 0,
                        )}
                    >
                        <SingleLineText
                            className={classes.xsdDefinitionStyles}
                            primary={schema.elements[id].name}
                        />
                    </ListItem>
                ))
            }

            <ListSubheader className={classes.listHeader}>Add attributes</ListSubheader>
            {
                menuAttributes.map(id => (
                    <ListItem
                        key={id}
                        button={true}
                        className={classes.sidebarItem}
                        onClick={() => addAttribute(
                            focusedElement!.id,
                            schema.attributes[id].name,
                            Object.keys(focusedElement!.attributes).length,
                        )}
                    >
                        <SingleLineText
                            className={classes.xsdDefinitionStyles}
                            primary={schema.attributes[id].name}
                        />
                    </ListItem>
                ))
            }
        </List>

        {root &&
            <List className={classNames(classes.editor, classes.scroll)}>
                <XsdElementView element={root} index={0}/>
            </List>
        }
    </Paper>
);

// We use a component class because react-dnd needs refs and stateless
// functions cannot be given ones.
class XsdDocumentsEditor extends React.PureComponent<
    XsdDocumentsEditorProps &
    XsdDocumentsEditorDispatch &
    WithStyles<keyof typeof styles>
> {
    render() {
        return <XsdDocumentsEditorView {...this.props}/>;
    }
}
/* tslint:enable:max-line-length */

const StyledDocumentsEditor = withStyles(styles)(XsdDocumentsEditor) as
    React.ComponentType<XsdDocumentsEditorProps & XsdDocumentsEditorDispatch>;
export default DragDropContext(Html5Backend)(StyledDocumentsEditor);
