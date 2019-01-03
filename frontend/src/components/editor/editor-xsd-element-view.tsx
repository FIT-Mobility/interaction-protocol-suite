import { XsdAttribute, XsdElement } from '@ips/shared-js';
import Button from '@material-ui/core/es/Button';
import ListItemIcon from '@material-ui/core/es/ListItemIcon';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import Tooltip from '@material-ui/core/es/Tooltip';
import Add from '@material-ui/icons/Add';
import Clear from '@material-ui/icons/Clear';
import ListIcon from '@material-ui/icons/List';
import classNames from 'classnames';
import { Dictionary } from 'lodash';
import * as React from 'react';
import {
    ConnectDragPreview,
    ConnectDragSource,
    ConnectDropTarget,
    DragSource,
    DragSourceSpec,
    DropTarget,
    DropTargetSpec,
} from 'react-dnd';
import { findDOMNode } from 'react-dom';

import AttributeView from '../../containers/editor/editor-xsd-attribute-view';
import XsdView from '../../containers/editor/editor-xsd-element-view';
import { ValidationError } from '../../state';
import { hidden } from '../utils/flex-primitives';
import { row } from '../utils/flex-primitives';
import SingleLineText from '../utils/single-line-listitem';

import { XsdAttributeView } from './editor-xsd-attribute-view';

export interface XsdElementViewProps extends XsdElementViewOwnProps {
    attributes: XsdAttribute[];
    canContainText: boolean;
    isFocused: boolean;
    title: string;
    validationError: ValidationError[] | null;
    xsd: Dictionary<XsdElement>;
    disableEditing: boolean;
}

export interface XsdElementViewOwnProps {
    element: XsdElement;
    index: number;
}

export interface XsdElementViewDispatch {
    addAttribute: (elId: string, name: string, position: number) => void;
    editContent: (ev: React.ChangeEvent<HTMLTextAreaElement>) => void;
    focusNode: () => void;
    moveNode: (elId: string, newPos: number) => void;
    removeNode: () => void;
}

interface DragProps {
    connectDragPreview: ConnectDragPreview;
    connectDragSource: ConnectDragSource;
    isDragging: boolean;
}

interface DropProps {
    connectDropTarget: ConnectDropTarget;
}

// tslint:disable:object-literal-key-quotes
export const xsdDefinitionStyles = {
    padding: 0,
    '& h3, span': {
        color: '#7f0000',
        fontFamily: 'monospace',
        fontSize: 16,
        fontWeight: 400,
    }
};

const styles = theme => createStyles({
    childrenContainer: {
        display: 'flex',
        flexFlow: 'column nowrap',
        marginLeft: theme.spacing.unit * 3,
        position: 'relative',
    },
    closingTag: {
        marginLeft: theme.spacing.unit * 5,
    },
    hasError: {
        border: '2px dotted red',
    },
    inlineAttrBtn: {
        marginLeft: 12,
        minWidth: 20,
        minHeight: 20,
        width: 20,
        height: 20,
        padding: 0,
        boxShadow: 'none',

        '& svg': {
            width: 20,
            height: 20,
        },
    },
    item: {
        display: 'inline-flex',
        alignItems: 'center',
        marginTop: 2,
        marginBottom: 2,
        cursor: 'pointer',
    },
    row,
    hidden,
    textNode: {
        appearance: 'none',
        background: 'white',
        border: 'none',
        cursor: 'text',
        fontFamily: 'monospace',
        fontSize: theme.spacing.unit * 2,
        fontWeight: 400,
        lineHeight: '1.5em',
        minHeight: '1.5em',
        margin: `0 ${theme.spacing.unit * 2}px 0 ${theme.spacing.unit * 5}px`,
        resize: 'vertical',
    },
    xsdDefinition: xsdDefinitionStyles,
    xsdDefinitionFocused: {
        '& h3, span': {
            fontWeight: 600,
        }
    }
});

type Props = XsdElementViewProps &
    XsdElementViewDispatch &
    { className?: string } &
    WithStyles<typeof styles>;

const Inner = (props: Props & WithStyles<typeof styles>) => {
    if (Array.isArray(props.element.content)) {
        const XSDView = XsdView as any;
        return props.element.content.map((id, idx) => (
            <XSDView
                key={id}
                className={props.className}
                element={props.xsd[id]}
                index={idx}
            />
        ));
    } else if (props.canContainText) {
        return (
            <textarea
                className={classNames(props.className, props.classes.textNode)}
                onChange={props.editContent}
                rows={1}
                value={props.element.content! || ''}
            />
        );
    } else {
        return null;
    }
};

class XsdElementView extends React.PureComponent<Props, Dictionary<XsdAttributeView>> {
    /* tslint:disable:max-line-length */
    render() {
        const {
            attributes,
            className,
            classes,
            element,
            isFocused,
            validationError,
            disableEditing,

            connectDragPreview,
            connectDragSource,
            connectDropTarget,
            focusNode,
            removeNode,
        } = this.props as Props & DragProps & DropProps;

        const errorText = validationError
            ? validationError.map(err => err.message).join('\n')
            : '';

        const dom = (
            <div className={classNames(className, { [classes.hasError]: validationError })}>
                <Tooltip title={errorText} enterDelay={1500}>
                    <span className={classes.item}>
                        <ListItemIcon className={classNames({ [classes.hidden]: disableEditing })}>
                            {connectDragSource(<div><ListIcon/></div>)}
                        </ListItemIcon>

                        <SingleLineText
                            className={classNames(
                                classes.xsdDefinition,
                                { [classes.xsdDefinitionFocused]: isFocused }
                            )}
                            onClick={focusNode}
                            primary={`<${element.name}`}
                        />

                        <Button
                            className={classNames(
                                classes.inlineAttrBtn,
                                { [classes.hidden]: disableEditing }
                            )}
                            variant="fab"
                            size="small"
                            title="Add attribute"
                            onClick={() => this.createNewAttribute(0)}
                        >
                            <Add/>
                        </Button>

                        {attributes.map((attr, idx) => (
                            <div className={classes.row} key={attr.id}>
                                <AttributeView
                                    element={element}
                                    attributeId={attr.id}
                                    isFocused={isFocused}
                                    disableEditing={disableEditing}
                                    onExitValueRight={() => this.focusTitle(idx + 1)}
                                    onExitTitleLeft={() => this.focusValue(idx - 1)}
                                    onRef={ref => this.setState({[attr.id]: ref})}
                                />
                                <Button
                                    className={classNames(
                                        classes.inlineAttrBtn,
                                        { [classes.hidden]: disableEditing }
                                    )}
                                    variant="fab"
                                    size="small"
                                    title="Add attribute"
                                    onClick={() => this.createNewAttribute(idx + 1)}
                                >
                                    <Add/>
                                </Button>
                            </div>
                        ))}

                        <SingleLineText
                            className={classNames(
                                classes.xsdDefinition,
                                { [classes.xsdDefinitionFocused]: isFocused }
                            )}
                            onClick={focusNode}
                            primary={">"}
                        />

                        {element.parentId && // Do not show remove button for root node
                            <Button
                                className={classNames(
                                    classes.inlineAttrBtn,
                                    { [classes.hidden]: disableEditing }
                                )}
                                variant="fab"
                                size="small"
                                title="Remove Node"
                                onClick={removeNode}
                            >
                                <Clear/>
                            </Button>}
                    </span>
                </Tooltip>

                <div className={classes.childrenContainer}>{Inner(this.props)}</div>

                <Tooltip title={errorText} enterDelay={1500}>
                    <span
                        className={classes.item}
                        onClick={focusNode}
                    >
                        <SingleLineText
                            className={classNames(
                                classes.xsdDefinition,
                                { [classes.xsdDefinitionFocused]: isFocused },
                                classes.closingTag
                            )}
                            primary={`</${element.name}>`}
                        />
                    </span>
                </Tooltip>
            </div>
        );

        return connectDragPreview(connectDropTarget(dom));
    }

    /* tslint:enable */

    private createNewAttribute(at: number) {
        const { addAttribute, element } = this.props;
        addAttribute(element.id, ' ', at);
        setTimeout(() => this.focusTitle(at), 20);
    }

    private focusTitle(index: number) {
        if (index < this.props.attributes.length) {
            this.state[this.props.attributes[index].id].focusTitle();
            return;
        }

        this.createNewAttribute(index);
    }

    private focusValue(index: number) {
        if (index < 0) {
            return;
        }

        this.state[this.props.attributes[index].id].focusValue();
    }
}

const StyledXsdView = withStyles(styles)(XsdElementView);

// React DnD Handling

const elementSource: DragSourceSpec<Props> = {
    beginDrag: (props: Props) => ({
        elementId: props.element.id,
        index: props.index,
        parentId: props.element.parentId,
    }),
};

const elementTarget: DropTargetSpec<Props> = {
    canDrop(props, monitor) {
        if (!monitor) {
            throw new Error("Missing monitor!");
        }

        const {parentId} = monitor.getItem() as any;
        return props.element.id === parentId;
    },

    hover(props, monitor, component) {
        if (!monitor || !component) {
            throw new Error("Missing monitor / component");
        }

        if (!monitor.isOver({shallow: true})) {
            return;
        }

        const {elementId, index: dragIndex} = monitor.getItem() as any;
        const hoverIndex = props.index;

        // Don't replace items with themselves
        if (dragIndex === hoverIndex) {
            return;
        }

        // Determine rectangle on screen and mouse middle
        const hoverBoundingRect = (findDOMNode(component) as Element).getBoundingClientRect();
        const hoverMiddleY = (hoverBoundingRect.bottom - hoverBoundingRect.top) / 2;
        const clientOffset = monitor.getClientOffset();

        // Get pixels to the top
        const hoverClientY = clientOffset.y - hoverBoundingRect.top;

        // Only perform the move when the mouse has crossed half of the items height
        // When dragging downwards, only move when the cursor is below 50%
        // When dragging upwards, only move when the cursor is above 50%

        // Dragging downwards
        if (dragIndex < hoverIndex && hoverClientY < hoverMiddleY) {
            return;
        }

        // Dragging upwards
        if (dragIndex > hoverIndex && hoverClientY > hoverMiddleY) {
            return;
        }

        // Time to actually perform the action
        props.moveNode(elementId, hoverIndex);

        // Note: we're mutating the monitor item here!
        // Generally it's better to avoid mutations,
        // but it's good here for the sake of performance
        // to avoid expensive index searches.
        (monitor.getItem() as any).index = hoverIndex;
    },
};

const DragSourceView = DragSource<Props>(
    'XML_ELEMENT',
    elementSource,
    (connect, monitor): DragProps => ({
        connectDragPreview: connect.dragPreview(),
        connectDragSource: connect.dragSource(),
        isDragging: monitor.isDragging(),
    }),
)(StyledXsdView as React.ComponentType<Props>);

export default DropTarget<Props>(
    'XML_ELEMENT',
    elementTarget,
    (connect): DropProps => ({ connectDropTarget: connect.dropTarget() }),
)(DragSourceView);
