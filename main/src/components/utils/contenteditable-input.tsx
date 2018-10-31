import * as React from 'react';

interface ContenteditableInputProps extends React.DetailedHTMLProps<
    React.HTMLAttributes<HTMLSpanElement>,
    HTMLSpanElement
> {
    className?: string;
}

/* tslint:disable:variable-name */

/**
 * Provides a contenteditable span that ensures it can be focused by embedding an
 * empty span element inside it, when the contents are empty.
 */
class ContenteditableInput extends React.Component<ContenteditableInputProps> {
    private _el: React.RefObject<HTMLSpanElement> = React.createRef();

    get childNodes(): NodeList {
        return this._el.current!.childNodes;
    }

    get innerText(): string {
        return this._el.current!.innerText;
    }

    get textContent(): string | null {
        return this._el.current!.textContent;
    }

    componentDidMount() {
        this.fixEmptyContent();
    }

    render() {
        return (
            <span
                {...this.props}
                className={this.props.className}
                contentEditable={this.props.contentEditable}
                onFocus={this.handleFocus}
                onKeyUp={this.handleKeyUp}
                onPaste={this.handlePaste}
                ref={this._el}
                suppressContentEditableWarning={true}
            >
                {this.props.children}
            </span>
        );
    }

    private fixEmptyContent() {
        if (this._el.current!.textContent !== '') {
            return;
        }

        this._el.current!.innerHTML = '';
        const s = document.createElement('span');
        s.textContent = ' ';
        this._el.current!.appendChild(s);
    }

    private handleFocus = (ev: React.FocusEvent<HTMLSpanElement>) => {
        this.fixEmptyContent();
        return typeof this.props.onFocus === 'function'
            ? this.props.onFocus(ev)
            : true;
    }

    private handleKeyUp = (ev: React.KeyboardEvent<HTMLSpanElement>) => {
        if (this._el.current!.textContent!.trim() !== '') {
            return typeof this.props.onKeyUp === 'function'
                ? this.props.onKeyUp(ev)
                : true;
        }

        this.fixEmptyContent();

        const range = document.createRange();
        range.setStart(this._el.current!, 0);
        range.collapse(true);
        const sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);

        return typeof this.props.onKeyUp === 'function'
            ? this.props.onKeyUp(ev)
            : true;
    }

    private handlePaste = (ev: React.ClipboardEvent<HTMLSpanElement>) => {
        this.fixEmptyContent();
        return typeof this.props.onPaste === 'function'
            ? this.props.onPaste(ev)
            : true;
    }
}

export default ContenteditableInput;
