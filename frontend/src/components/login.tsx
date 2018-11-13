import Button from '@material-ui/core/es/Button';
import Collapse from '@material-ui/core/es/Collapse';
import Divider from '@material-ui/core/es/Divider';
import LinearProgress from '@material-ui/core/es/LinearProgress';
import ListSubheader from '@material-ui/core/es/ListSubheader';
import { createStyles, withStyles, WithStyles } from '@material-ui/core/es/styles';
import TextField from '@material-ui/core/es/TextField';
import Email from '@material-ui/icons/Email';
import Face from '@material-ui/icons/Face';
import Lock from '@material-ui/icons/Lock';
import PermIdentity from '@material-ui/icons/PermIdentity';
import classNames from 'classnames';
import * as React from 'react';
import { Link } from 'redux-little-router';

import { LoginError } from '../state';

import Form from './utils/flex-paper';
import { column, row } from './utils/flex-primitives';

export interface LoginProps {
    canLogin: boolean;
    canRegister: boolean;
    emailInput: string;
    hasEmailFormatError: boolean;
    hasNetworkError: boolean;
    hasLoginError: boolean;
    hasPasswordMismatchError: boolean;
    hasRegistrationError: boolean;
    loginError: LoginError;
    loginInProgress: boolean;
    message: string | null;
    passwordInput: string;
    passwordRepeatInput: string;
    nameInput: string;
    isRegistering: boolean;
}

export interface LoginDispatch {
    emailInputChanged: (mail: string) => void;
    nameInputChanged: (name: string) => void;
    passwordInputChanged: (pw: string) => void;
    passwordRepeatInputChanged: (pw: string) => void;
    submit: () => void;
}

const invisibleChar = '\u2063';

const styles = theme => createStyles({
    body: {
        ...column,
        alignItems: 'stretch',
        padding: `${theme.spacing.unit * 2}px`,
    },
    head: {
        padding: `${theme.spacing.unit * 4}px`,
    },
    header: {
        textTransform: 'uppercase',
    },
    error: {
        color: '#ff1744',
    },
    input: {
        marginBottom: `${theme.spacing.unit * 2}px`,
        marginLeft: `${theme.spacing.unit * 2}px`,
    },
    largeFace: {
        marginRight: `${theme.spacing.unit * 4}px`,
        height: `${theme.spacing.unit * 8}px`,
        width: `${theme.spacing.unit * 8}px`,
    },
    row,
});

const EmailHelperText = (props: Props): React.ReactNode => {
    if (props.hasRegistrationError && !!props.loginError) {
        return <span>{props.loginError!.message}</span>;
    } else if (props.hasEmailFormatError) {
        return <span>Please enter a valid E-Mail address</span>;
    } else {
        return invisibleChar;
    }
};

function keyPressedHandler(props: Props) {
    return (ev: KeyboardEvent) => {
        if (ev.which !== 13) {
            return;
        }

        if (props.isRegistering && props.canRegister) {
            props.submit();
        }
        if (!props.isRegistering && props.canLogin) {
            props.submit();
        }
    };
}

const Message = (props: Props) => {
    if (!props.message) {
        return null;
    }

    return (
        <>
            <Divider/>
            <ListSubheader>{props.message}</ListSubheader>
        </>
    );
};

type Props = LoginProps &
    LoginDispatch &
    WithStyles<typeof styles>;

const Login = (props: Props) => (
    <Form style={{minHeight: props.isRegistering ? "513px" : "365px"}}>
        <div className={classNames(props.classes.row, props.classes.head)}>
            <Face className={props.classes.largeFace}/>
            <h3 className={props.classes.header}>{props.isRegistering ? "Register to OMP" : "Login to OMP"}</h3>
        </div>

        <Message {...props}/>
        <Divider/>

        <div className={classNames(props.classes.body)}>
            <div className={props.classes.row}>
                <Email/>
                <TextField
                    type="email"
                    className={props.classes.input}
                    label="E-Mail"
                    fullWidth={true}
                    helperText={EmailHelperText(props)}
                    error={props.hasEmailFormatError || props.hasRegistrationError}
                    onChange={(ev: any) => props.emailInputChanged(ev.target.value)}
                    onKeyPress={keyPressedHandler(props) as any}
                    value={props.emailInput}
                />
            </div>
            <div className={props.classes.row}>
                <Lock/>
                <TextField
                    type="password"
                    className={props.classes.input}
                    label="Password"
                    fullWidth={true}
                    helperText={props.hasLoginError
                        ? <span>
                            {props.loginError!.message} <Link href="/register">Do you wish to register?</Link>
                        </span>
                        : invisibleChar}
                    error={props.hasLoginError}
                    onChange={(ev: any) => props.passwordInputChanged(ev.target.value)}
                    onKeyPress={keyPressedHandler(props) as any}
                    value={props.passwordInput}
                />
            </div>
            <Collapse
                in={props.isRegistering}
                timeout="auto"
                unmountOnExit={true}
            >
                <div>
                    <div className={props.classes.row}>
                        <Lock/>
                        <TextField
                            type="password"
                            className={props.classes.input}
                            label="Password (repeat)"
                            fullWidth={true}
                            helperText={props.hasPasswordMismatchError
                                ? "Passwords not matching"
                                : invisibleChar}
                            error={props.hasPasswordMismatchError}
                            onChange={(ev: any) => props.passwordRepeatInputChanged(ev.target.value)}
                            onKeyPress={keyPressedHandler(props) as any}
                            value={props.passwordRepeatInput}
                        />
                    </div>

                    <div className={props.classes.row}>
                        <PermIdentity/>
                        <TextField
                            type="text"
                            className={props.classes.input}
                            label="Full name"
                            fullWidth={true}
                            onChange={(ev: any) => props.nameInputChanged(ev.target.value)}
                            onKeyPress={keyPressedHandler(props) as any}
                            value={props.nameInput}
                        />
                    </div>
                </div>
            </Collapse>

            <Button
                color="primary"
                onClick={props.submit}
                disabled={props.isRegistering ? !props.canRegister : !props.canLogin}
            >
                {props.isRegistering ? "Register" : "Login"}
            </Button>

            {props.hasNetworkError &&
                <span className={props.classes.error}>
                    Could not {props.isRegistering ? "register" : "login"}. Is your internet connection working?
                </span>
            }
        </div>

        {props.loginInProgress && <LinearProgress/>}
    </Form>
);

export default withStyles(styles)(Login);
