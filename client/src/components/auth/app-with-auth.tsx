import hoistNonReactStatics from 'hoist-non-react-statics';
import {WithRouterProps} from 'next/dist/client/with-router';
import {withRouter} from 'next/router';
import React from 'react';
import {AccessDeniedError, UnhandledApiError} from '../../api/api-exception';
import {apiFetch} from '../../api/api-fetch';
import {browserLocalStorage} from '../../utils/browser-storage';
import {catchAsyncError} from '../../utils/react-utils';
import {AuthGuard, ClientAuthGuard, SerializedAuthGuard} from './auth';
import {AuthContext} from './auth-context';

interface AppWithAuthProps {
    authGuard: SerializedAuthGuard;
}

export function appWithAuth() { // wrap everything into a function in the case we add an options param
    return (WrappedComponent) => {
        class AppWithAuth extends React.Component<AppWithAuthProps & WithRouterProps> {
            static async getInitialProps(ctx) {
                const {req} = ctx.ctx;
                let authGuard;
                if (req) {
                    // server-side auth guard initialization can be done here if needed later
                    // eg. authGuard = await fetchAuthByCookie();
                    authGuard = {};
                    ctx.ctx.authGuard = authGuard;
                } else {
                    ctx.ctx.authGuard = getGlobalAuthGuard();
                }

                let wrappedComponentProps;
                if (WrappedComponent.getInitialProps) {
                    wrappedComponentProps = await WrappedComponent.getInitialProps(ctx);
                }

                return {
                    authGuard,
                    ...wrappedComponentProps,
                };
            }

            authSync = false;
            authStore = {
                authenticating: true,
                authMethods: {login: undefined, logout: undefined},
                authGuard: new ClientAuthGuard(),
            };

            constructor(props: AppWithAuthProps & WithRouterProps, ctx: any) {
                super(props, ctx);
                this.authStore.authMethods.login = this.login;
                this.authStore.authMethods.logout = this.logout;
                if (props.authGuard) {
                    if (typeof window !== 'undefined') {
                        setGlobalAuthGuard(this.authStore.authGuard);
                    }
                    this.authStore.authGuard.update(props.authGuard, true);
                }
            }

            componentDidMount() {
                this.authStore.authGuard.addListener('updated', this.onAuthUpdated);
                window.addEventListener('storage', this.onAuthSync);

                const token = getGlobalAuthToken();
                if (token) {
                    apiFetch({
                        url: 'auth/remember',
                        authToken: token,
                    }).then((res) => {
                        if (res.success == false) {
                            throw new UnhandledApiError(res.error);
                        } else {
                            this.authStore.authenticating = false;
                            this.authStore.authGuard.update({token, ...res.result});
                        }
                    }).catch((err) => {
                        this.authStore.authenticating = false;
                        if (err instanceof AccessDeniedError && err.invalidToken) {
                            this.authStore.authenticating = false;
                            this.authStore.authGuard.update({});
                        } else {
                            this.forceUpdate();
                            catchAsyncError(this, err);
                        }
                    });
                } else {
                    this.authStore.authenticating = false;
                    this.forceUpdate();
                }
            }

            componentWillUnmount() {
                window.removeEventListener('storage', this.onAuthSync);
                this.authStore.authGuard.removeListener('updated', this.onAuthUpdated);
            }

            onAuthUpdated = (s: SerializedAuthGuard) => {
                if (window.localStorage && !this.authSync) {
                    this.authSync = true;
                    try {
                        setGlobalAuthToken(s ? s.token : undefined);
                        window.localStorage.setItem(authSyncKey, JSON.stringify(s ? s : null));
                        window.localStorage.removeItem(authSyncKey);
                    } finally {
                        this.authSync = false;
                    }
                }
                this.forceUpdate();
            };

            onAuthSync = (event: StorageEvent) => {
                if (!this.authSync) {
                    this.authSync = true;
                    try {
                        if (event.key !== authSyncKey || typeof event.newValue !== 'string') {
                            return;
                        }
                        const s = JSON.parse(event.newValue) as SerializedAuthGuard;
                        this.authStore.authGuard.update(s);
                    } finally {
                        this.authSync = false;
                    }
                }
            };

            login = (email: string, password: string) => {
                return apiFetch({
                    authToken: false,
                    url: 'auth/login',
                    body: {email, password},
                }).then((res) => {
                    if (res.success == false) {
                        if (res.error.code === 'INVALID_CREDENTIALS') {
                            return false;
                        }
                        throw new UnhandledApiError(res.error);
                    } else {
                        this.authStore.authGuard.update(res.result);
                        return true;
                    }
                });
            };

            logout = () => {
                return apiFetch({
                    url: 'auth/logout',
                    body: {},
                }).then((res) => {
                    if (res.success == false) {
                        throw new UnhandledApiError(res.error);
                    }
                    this.authStore.authGuard.update({});
                }).catch((err) => {
                    // the remote session deletion failed, but only removing the local state is enough
                    this.authStore.authGuard.update({});
                });
            };

            render() {
                return (
                    <AuthContext.Provider value={this.authStore}>
                        <WrappedComponent {...this.props}/>
                    </AuthContext.Provider>
                );
            }
        }

        return hoistNonReactStatics(withRouter(AppWithAuth), WrappedComponent, {getInitialProps: true});
    };
}

const authTokenKey = 'auth.token';
const authSyncKey = 'sync:auth.update';

function setGlobalAuthToken(value: string | undefined) {
    browserLocalStorage.set(authTokenKey, value);
}

function getGlobalAuthToken() {
    return browserLocalStorage.getChecked(authTokenKey, 'string');
}

function setGlobalAuthGuard(authGuard: AuthGuard) {
    if (typeof window === 'undefined') throw new Error('Global AuthGuard is client-side only');
    // @ts-ignore
    global.__AUTH_GUARD = authGuard;
}

export function getGlobalAuthGuard(): AuthGuard {
    // @ts-ignore
    if (!global.__AUTH_GUARD) throw new Error('Global AuthGuard is not defined');
    // @ts-ignore
    return global.__AUTH_GUARD;
}
