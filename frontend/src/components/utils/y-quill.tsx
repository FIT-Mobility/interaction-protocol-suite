import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import classNames from 'classnames';
import merge from 'lodash-es/merge';
import Quill, { QuillOptionsStatic, TextChangeHandler } from 'quill';
import * as React from 'react';
import { v4 as uuid } from 'uuid';

import { Types } from '../../actions';
import { getYProjectRevision } from '../../actions/sync/yjs';

import { column } from './flex-primitives';

const Inline = Quill.import('blots/inline');

class ReferenceBlot extends Inline {
    static create(refId: string): Element {
        const node: Element = super.create(refId);

        node.setAttribute('id', refId);
        node.classList.add('comment-reference');

        node.addEventListener('click', () => window.dispatchEvent(
            new CustomEvent(Types.ClickCommentReference, {detail: refId}),
        ));

        return node;
    }

    static formats(node: Element): string | null {
        return node.getAttribute('id');
    }

    static value(node: Element): string | null {
        return node.getAttribute('id');
    }
}

ReferenceBlot.blotName = 'ref';
ReferenceBlot.tagName = 'span';
Quill.register(ReferenceBlot);

export interface YQuillProps extends QuillOptionsStatic {
    className?: string;
    richtextId: string | null;
    disableEditing: boolean;

    onCreateReference?: (refId: string, text: string) => void;
    onTextChange?: TextChangeHandler;
}

const styles = createStyles({
    column,
    container: {
        alignItems: 'stretch',
        position: 'relative',
        height: '100%',
        width: '100%',
    },
});

class YQuill extends React.Component<YQuillProps & WithStyles<typeof styles>> {
    private area: HTMLDivElement | null;
    private editor: Quill;
    private richtextId: string;
    private richtextObj: any;
    private typeHandler: TextChangeHandler;

    componentWillReceiveProps(nextProps: Readonly<YQuillProps>): void {
        this.updateYBindings(nextProps);
    }

    componentDidMount(): void {
        const handler = this.props.onCreateReference;
        /* tslint:disable:object-literal-key-quotes */
        const toolbarOpts = {
            container: [
                [{'header': [1, 2, 3, 4, 5, 6, false]}, {'font': []}],
                [{'list': 'ordered'}, {'list': 'bullet'}],
                ['bold', 'italic', 'underline', 'strike'],
                ['link', 'image'],
                [
                    {'color': []},
                    {
                        'background': [
                            '#000000',
                            '#404040',
                            '#c0c0c0',
                            '#ffffff',
                            '#0000ff',
                            '#000080',
                            '#00ffff',
                            '#008b8b',
                            '#00ff00',
                            '#003300',
                            '#ff00ff',
                            '#8b008b',
                            '#ff0000',
                            '#8b0000',
                            '#ffff00',
                            '#808000',
                        ],
                    },
                ],
                [{'script': 'sub'}, {'script': 'super'}],
                [{'align': []}, {'indent': '-1'}, {'indent': '+1'}],
                ['ref'],
                ['clean'],
            ],
            handlers: {
                'ref'(this: { quill: Quill }, value) {
                    if (value === true) { // IDs should be permanent
                        this.quill.format('ref', uuid());
                    }

                    const sel = this.quill.getSelection()!;
                    const txt = this.quill.getText(sel.index, sel.length);
                    const formats = this.quill.getFormat(sel);
                    handler && handler(formats.ref, txt);
                },
            },
        };
        /* tslint:enable */

        this.editor = new Quill(this.area!, merge({
            modules: {toolbar: toolbarOpts},
            theme: 'snow',
        }, this.props));

        this.updateYBindings(this.props);

        if (this.props.disableEditing) {
            this.editor.disable();
        }
    }

    componentWillUnmount() {
        this.unbindY();
        this.editor.off('text-change', this.typeHandler);

        // Clean up the automatically generated toolbar element
        const tb = this.editor.getModule('toolbar');
        tb.container.remove();
    }

    render() {
        // Container required for proper toolbar placement.

        const {column, container} = this.props.classes;
        return (
            <div className={classNames(column, container)}>
                <div className={this.props.className} ref={el => this.area = el}/>
            </div>
        );
    }

    updateYBindings(nextProps: Readonly<YQuillProps>): void {
        this.editor.off('text-change', this.typeHandler);
        this.typeHandler = nextProps.onTextChange || (() => {
        });
        this.editor.on('text-change', this.typeHandler);

        if (nextProps.richtextId === this.richtextId) {
            return;
        }

        this.unbindY();

        if (!nextProps.richtextId) {
            return;
        }

        const projectRevision = getYProjectRevision();
        if (!projectRevision) {
            // This can occur while the project is loading. Assume that we'll get
            // another props update shortly and do nothing this round.
            return;
        }

        const obj = projectRevision.share.docTexts.get(nextProps.richtextId);
        if (!obj) {
            throw new Error("Missing docs object!");
        }
        this.richtextId = nextProps.richtextId!;

        this.richtextObj = obj;
        obj.bind(this.editor);
    }

    unbindY(): void {
        if (this.richtextObj && this.editor) {
            this.richtextObj.unbindQuill(this.editor);
        }

        this.richtextId = '';
        this.richtextObj = null;
    }
}

export default withStyles(styles)(YQuill);
