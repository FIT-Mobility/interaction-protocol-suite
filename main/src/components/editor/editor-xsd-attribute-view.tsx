import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import Tooltip from '@material-ui/core/es/Tooltip';
import classNames from 'classnames';
import { XsdAttribute, XsdElement } from 'omp-schema';
import * as React from 'react';

import { ValidationError } from '../../state';
import ContenteditableInput from '../utils/contenteditable-input';

export interface XsdAttributeViewProps extends XsdAttributeViewOwnProps {
    attribute: XsdAttribute;
    validationError: ValidationError[] | null;
}
export interface XsdAttributeViewOwnProps {
    attributeId: string;
    element: XsdElement;
    isFocused: boolean;

    onExitTitleLeft?: () => void;
    onExitValueRight?: () => void;
    onRef?: (inst: XsdAttributeView) => void;
    disableEditing: boolean;
}
export interface XsdAttributeViewDispatch {
    editName: (elId: string, attributeId: string, name: string) => void;
    editValue: (elId: string, attributeId: string, name: string) => void;
    removeAttribute: (elId: string, attributeId: string) => void;
}
interface XsdAttributeViewState {
    name: string;
    originalName: string;
    value: string;
}

const styles = theme => createStyles({
    attributeText: {
        background: 'none',
        border: 'none',
        color: 'black',

        fontFamily: 'monospace',
        fontSize: theme.spacing.unit * 2,
        fontWeight: 400,
        lineHeight: `${theme.spacing.unit * 3}px`,
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
    },
    container: {
        display: 'flex',
        flexFlow: 'row nowrap',
        margin: '0 0 0 12px',
    },
    editable: {
        cursor: 'text',
    },
    editableFocused: {
        fontWeight: 600,
    },
    invalid: {
        borderBottom: '2px dotted red',
    },
    title: {
        color: '#ff0000',
    },
    value: {
        color: '#0000ff',
    },
});

type Props = XsdAttributeViewProps &
    XsdAttributeViewDispatch &
    WithStyles<typeof styles> &
    { className?: string };

export class XsdAttributeView extends React.PureComponent<Props, XsdAttributeViewState> {
    private static isValidAttributeName(name: string): boolean {
        return /^[a-zA-Z_:]([a-zA-Z0-9_:.-])*$/.test(name);
    }

    private static placeCaretIn(el: ContenteditableInput | HTMLSpanElement, offset: number) {
        const range = document.createRange();
        range.setStart(el.childNodes[0], offset);

        const sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    }

    private titleRef: React.RefObject<ContenteditableInput> = React.createRef();
    private valueRef: React.RefObject<ContenteditableInput> = React.createRef();

    constructor(props: Props, context?: any) {
        super(props, context);

        this.state = {
            name: '',
            originalName: '',
            value: '',
        };
    }

    focusTitle() {
        XsdAttributeView.placeCaretIn(this.titleRef.current!, 0);
    }

    focusValue() {
        XsdAttributeView.placeCaretIn(
            this.valueRef.current!,
            (this.valueRef.current!.textContent || '').length,
        );
    }

    componentDidMount() {
        if (typeof this.props.onRef === 'function') {
            this.props.onRef(this);
        }

        this.componentWillReceiveProps(this.props, null);
    }

    componentWillReceiveProps(nextProps: Props, nextContext: any) {
        this.setState({
            name: nextProps.attribute.name,
            originalName: nextProps.attribute.name,
            value: nextProps.attribute.value,
        });
    }

    render() {
        const errorText = this.props.validationError
            ? this.props.validationError.map(err => err.message).join('\n')
            : '';
        return (
            <Tooltip
                className={this.props.className}
                title={errorText}
                enterDelay={1000}
            >
                <div
                    className={classNames(
                        this.props.classes.container,
                        { [this.props.classes.invalid]: this.props.validationError },
                    )}
                >
                    <ContenteditableInput
                        className={classNames(
                            this.props.classes.attributeText,
                            this.props.classes.editable,
                            { [this.props.classes.editableFocused]: this.props.isFocused },
                            this.props.classes.title,
                        )}
                        contentEditable={!this.props.disableEditing}
                        onBlur={this.handleTitleChange}
                        onKeyDown={this.handleTitleKeyDown}
                        onPaste={this.handleTitlePaste}
                        ref={this.titleRef as any}
                        suppressContentEditableWarning={true}
                    >
                        {this.state.name}
                    </ContenteditableInput>
                    <span
                        className={this.props.classes.attributeText}
                        onClick={this.focusValue}
                    >
                        {'="'}
                    </span>
                    <ContenteditableInput
                        className={classNames(
                            this.props.classes.attributeText,
                            this.props.classes.editable,
                            { [this.props.classes.editableFocused]: this.props.isFocused },
                            this.props.classes.value,
                        )}
                        contentEditable={!this.props.disableEditing}
                        onBlur={this.handleValueChange}
                        onKeyDown={this.handleValueKeyDown}
                        onPaste={this.handleValuePaste}
                        ref={this.valueRef as any}
                        suppressContentEditableWarning={true}
                    >
                        {this.state.value}
                    </ContenteditableInput>
                    <span
                        className={this.props.classes.attributeText}
                        onClick={this.focusValue}
                    >
                        {'"'}
                    </span>
                </div>
            </Tooltip>
        );
    }

    private handleTitleChange = (ev: React.ChangeEvent<HTMLSpanElement>) => {
        const name = ev.target.textContent || '';
        this.setState({ name });

        const { attribute, element } = this.props;
        if (!name.trim() && !this.state.value.trim()) {
            this.props.removeAttribute(element.id, attribute.id);
            return;
        }

        if (name === this.state.originalName) {
            return;
        }

        this.props.editName(
            element.id,
            attribute.id,
            name,
        );
    }

    private handleTitleKeyDown = (ev: React.KeyboardEvent<HTMLSpanElement>) => {
        const sel = document.getSelection()!;

        // Don't do anything if either:
        // - We have a range selected
        // - The user didn't press arrow-right, arrow-left or tab
        // - The user pressed arrow-right, but we're not at the end of the input
        // - The user pressed arrow-left, but we're not at the start of the input
        // - The user entered something that turns

        const isArrowRightAndEnd = ev.key === 'ArrowRight' &&
            sel.baseOffset === (this.titleRef.current!.textContent || '').length;
        const isArrowLeftAndStart = ev.key === 'ArrowLeft' && sel.baseOffset === 0;
        const isShiftTab = ev.key === 'Tab' && ev.shiftKey;
        const isJustTab = ev.key === 'Tab' && !ev.shiftKey;
        const isJustShift = ev.key === 'Shift';
        const isEnter = ev.key === 'Enter';

        const content = (ev.target as HTMLSpanElement).innerText || '';

        // ev.key can also be things like 'ArrowLeft', so filter these events out first
        // before checking whether the attribute name is valid.
        const attrName = `${content.substring(0, sel.baseOffset)}${ev.key}${content.substr(sel.baseOffset)}`;
        if (isEnter ||
            (ev.key !== 'Backspace' &&
            ev.key !== 'ArrowLeft' &&
            ev.key !== 'ArrowRight' &&
            !isJustTab && !isJustShift &&
            !XsdAttributeView.isValidAttributeName(attrName))) {
            ev.preventDefault();
            return false;
        }

        if (typeof this.props.onExitTitleLeft === 'function' && (isArrowLeftAndStart || isShiftTab)) {
            ev.preventDefault();
            this.props.onExitTitleLeft();
        } else if (isArrowRightAndEnd || isJustTab) {
            ev.preventDefault();
            XsdAttributeView.placeCaretIn(this.valueRef.current!, 0);
        }

        return true;
    }

    private handleTitlePaste = (ev: React.ClipboardEvent<HTMLSpanElement>) => {
        const content = ev.clipboardData.getData('text');
        if (content && !XsdAttributeView.isValidAttributeName(content)) {
            ev.preventDefault();
            return false;
        }

        return true;
    }

    private handleValueChange = (ev: React.ChangeEvent<HTMLSpanElement>) => {
        const value = ev.target.textContent || '';
        this.setState({ value });

        const { attribute, element } = this.props;
        if (!this.state.name.trim() && !value.trim()) {
            this.props.removeAttribute(element.id, attribute.id);
            return;
        }
        this.props.editValue(
            element.id,
            attribute.id,
            value,
        );
    }

    private handleValueKeyDown = (ev: React.KeyboardEvent<HTMLSpanElement>) => {
        const sel = document.getSelection()!;

        // Don't do anything if either:
        // - We have a range selected
        // - The user didn't press arrow-left, arrow-right or tab
        // - The user pressed arrow-right, but we're not at the end of the input
        // - The user pressed arrow-left, but we're not at the start of the input
        // - The user pressed tab, but didn't press shift as well

        if (sel.type !== 'Caret') {
            return;
        }

        const isArrowLeftAndStart = ev.key === 'ArrowLeft' && sel.baseOffset === 0;
        const isArrowRightAndEnd = ev.key === 'ArrowRight' &&
            sel.baseOffset === (this.valueRef.current!.textContent || '').length;
        const isShiftTab = ev.key === 'Tab' && ev.shiftKey;
        const isJustTab = ev.key === 'Tab' && !ev.shiftKey;
        const isEnter = ev.key === 'Enter';

        if (isEnter) {
            ev.preventDefault();
            return false;
        }

        if (isShiftTab || isArrowLeftAndStart) {
            ev.preventDefault();
            XsdAttributeView.placeCaretIn(
                this.titleRef.current!,
                (this.titleRef.current!.textContent || '').length,
            );
        } else if (typeof this.props.onExitValueRight === 'function' && (isArrowRightAndEnd || isJustTab)) {
            ev.preventDefault();
            this.props.onExitValueRight();
        }

        return true;
    }

    private handleValuePaste = (ev: React.ClipboardEvent<HTMLSpanElement>) => {
        const content = ev.clipboardData.getData('text');
        if (content && content.indexOf("\n") !== -1) {
            ev.preventDefault();
            return false;
        }

        return true;
    }
}

export default withStyles(styles)(XsdAttributeView);
