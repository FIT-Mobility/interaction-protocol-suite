import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { connect } from 'react-redux';

import { focusComponent } from '../../actions/components/with-focus';
import { State } from '../../state';
import { store } from "../../store";

export interface WithFocusProps {
    focusedComponent: string | null;
    focusId: string;
    passProps: any;
}

interface WithFocusDispatch {
    focusComponent: (focusId: string | null) => void;
}

const mapStateToProps = (state: State, { focusId, ...rest }: any): WithFocusProps => ({
    focusedComponent: state.components.focusedComponent,
    focusId,
    passProps: rest,
});

const mapDispatchToProps: WithFocusDispatch = {
    focusComponent,
};

type Props = WithFocusProps & WithFocusDispatch;

export default function WithFocus<P>(Comp: React.ComponentClass<P>) {
    class Wrapped extends React.Component<P & Props> {
        private ref = React.createRef();
        private domNode: HTMLElement;

        componentDidMount() {
            this.domNode = (ReactDOM.findDOMNode(this.ref.current! as any) as HTMLElement);
            this.domNode = this.domNode.querySelector('input, textarea, button') as HTMLElement;

            this.domNode.addEventListener('focus', this.focusHandler);
            this.domNode.addEventListener('blur', this.blurHandler);
        }

        componentWillUnmount() {
            this.domNode.removeEventListener('focus', this.focusHandler);
            this.domNode.removeEventListener('blur', this.blurHandler);
        }

        componentDidUpdate(prevProps: Readonly<P & Props>) {
            const focusId = this.props.focusId as string;

            if (prevProps.focusedComponent !== this.props.focusedComponent &&
                this.props.focusedComponent === focusId) {
                this.focus();
            } else if (prevProps.focusedComponent === focusId &&
                this.props.focusedComponent === null) {
                this.unfocus();
            }
        }

        render() {
            return <Comp {...this.props.passProps} ref={this.ref}/>;
        }

        private focusHandler = () => {
            store.dispatch(focusComponent(this.props.focusId));
        }
        private blurHandler = () => {
            store.dispatch(focusComponent(null));
        }

        private focus() {
            this.domNode.focus();
        }
        private unfocus() {
            this.domNode.blur();
        }
    }

    return connect(mapStateToProps, mapDispatchToProps)(Wrapped);
}
