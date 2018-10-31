import { connect } from 'react-redux';

import {
    emailInputChanged, nameInputChanged,
    passwordInputChanged,
    passwordRepeatInputChanged,
    submit,
} from '../actions/components/login';
import LoginView, { LoginDispatch, LoginProps } from '../components/login';
import { LoginErrorType, State } from '../state';

/* tslint:disable-next-line:max-line-length */
const emailRegex = /(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/;

const mapStateToProps = (state: State): LoginProps => {
    const login = state.components.login;
    const canLogin = login.userEmail && login.userPassword && emailRegex.test(login.userEmail);
    const passwordsMatch = login.userPassword === login.userPasswordRepeat;
    const isRegistering = state.router.result ? state.router.result.isRegistering : false;
    return {
        canLogin: !!canLogin,
        canRegister: !!(canLogin && passwordsMatch && login.userName),
        emailInput: login.userEmail,
        hasEmailFormatError: !!login.userEmail && !emailRegex.test(login.userEmail),
        hasNetworkError: !!login.error && login.error.cause === LoginErrorType.Network,
        hasLoginError: !!login.error && login.error.cause === LoginErrorType.Login,
        hasRegistrationError: !!login.error && login.error.cause === LoginErrorType.Registration,
        hasPasswordMismatchError: !!login.userPasswordRepeat && login.userPassword !== login.userPasswordRepeat,
        loginError: login.error!,
        loginInProgress: login.inProgress,
        message: state.components.login.message,
        passwordInput: login.userPassword,
        nameInput: login.userName,
        passwordRepeatInput: login.userPasswordRepeat,
        isRegistering,
    };
};
const mapDispatchToProps: LoginDispatch = {
    emailInputChanged,
    nameInputChanged,
    passwordInputChanged,
    passwordRepeatInputChanged,
    submit,
};

const Login = connect(
    mapStateToProps,
    mapDispatchToProps,
)(LoginView);

export default Login;
