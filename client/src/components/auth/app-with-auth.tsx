import hoistNonReactStatics from 'hoist-non-react-statics';
import JsCookies from 'js-cookie';
import {AppContext} from 'next/app';
import {WithRouterProps} from 'next/dist/client/with-router';
import {NextPageContext} from 'next/dist/next-server/lib/utils';
import React from 'react';
import {catchAsyncError} from '../../utils/react-utils';
import {AuthGuard, ClientAuthGuard, ClientAuthGuardData, ServerAuthGuard, UpdateAuthContext} from './auth';
import {AuthContext} from './auth-context';

export interface AppWithAuthContext extends AppContext {
    ctx: NextPageWithAuthContext;
    cookies?: { get(name: string): void; set(name: string, value?: string, options?: any): void };
}

export interface NextPageWithAuthContext extends NextPageContext {
    authenticating: boolean;
    authGuard: AuthGuard;
}

interface AppWithAuthProps {
    authGuard: ClientAuthGuardData;
}

export function appWithAuth() { // wrap everything into a function in the case we add an options param
    const initSsrCookiesIfNeeded = (appCtx: AppWithAuthContext) => {
        if (typeof window === 'undefined' && !appCtx.cookies) {
            appCtx.cookies = new (require('cookies'))(appCtx.ctx.req, appCtx.ctx.res);
        }
    };
    return (WrappedAppComponent) => {
        // noinspection JSPotentiallyInvalidUsageOfThis
        class AppWithAuth extends React.Component<AppWithAuthProps & WithRouterProps, any> {
            static async getInitialProps(appCtx: AppWithAuthContext) {
                let authProps;
                if ((appCtx.Component as any).isPageWithAuth === true) {
                    const {req} = appCtx.ctx;
                    let ssrAuthGuard;
                    if (req) {
                        ssrAuthGuard = new ServerAuthGuard();
                        appCtx.ctx.authenticating = true;
                        appCtx.ctx.authGuard = ssrAuthGuard;
                        if (true) { // TODO: add an option to enable/disable SSR authentication
                            initSsrCookiesIfNeeded(appCtx);
                            const token = loadAuthToken(appCtx);
                            if (token) {
                                let remembered;
                                try {
                                    remembered = !!await ssrAuthGuard.remember(token);
                                    appCtx.ctx.authenticating = false;
                                } catch (e) {
                                    console.error('SSR remember thrown:', e);
                                }
                                if (remembered === false) {
                                    saveAuthToken(undefined, appCtx);
                                }
                            }
                        }
                    } else {
                        appCtx.ctx.authenticating = false;
                        appCtx.ctx.authGuard = getGlobalAuthGuard();
                    }
                    authProps = {
                        authGuard: ssrAuthGuard ? ssrAuthGuard.serialize() : undefined,
                    };
                }

                let wrappedComponentProps;
                if (WrappedAppComponent.getInitialProps) {
                    wrappedComponentProps = await WrappedAppComponent.getInitialProps(appCtx);
                }

                return {
                    ...authProps,
                    ...wrappedComponentProps,
                };
            }

            private _authInitialized = false;
            private _authSync = false;
            private _authGuard = new ClientAuthGuard();

            constructor(props: AppWithAuthProps & WithRouterProps, ctx: any) {
                super(props, ctx);
                this._authGuard.deserialize(props.authGuard, {noEmit: true});
                let authenticating = false;
                if (typeof window !== 'undefined') {
                    setGlobalAuthGuard(this._authGuard);
                    authenticating = this._authGuard.token !== loadAuthToken();
                }
                this.state = {authenticating, authGuard: this._authGuard.clone()};
            }

            componentDidMount() {
                this._initAuth();
            }

            componentDidUpdate(prevProps: Readonly<AppWithAuthProps & WithRouterProps>) {
                this._initAuth();
            }

            componentWillUnmount() {
                window.removeEventListener('storage', this._onAuthSync);
                this._authGuard.removeListener('updated', this._onAuthUpdated);
            }

            private _initAuth() {
                if (this._authInitialized || (this.props as any).Component.isPageWithAuth !== true) {
                    return;
                }
                this._authInitialized = true;

                this._authGuard.addListener('updated', this._onAuthUpdated);
                window.addEventListener('storage', this._onAuthSync);

                const remember = (): Promise<any> => {
                    const token = loadAuthToken();
                    if (this._authGuard.token !== token) {
                        if (token) {
                            // Not remembered in SSR, do client-side remembering
                            return this._authGuard.remember(token).catch((err) => catchAsyncError(this, err));
                        } else {
                            // Remembered in SSR, but disconnected since
                            this._authGuard.deserialize({});
                        }
                    }
                    return Promise.resolve();
                };
                remember().finally(() => {
                    this.setState({authenticating: false});
                });
            }

            private _onAuthUpdated = (data: ClientAuthGuardData, prevData: ClientAuthGuardData, ctx: UpdateAuthContext) => {
                // this.authStore.authenticating = false;
                if (window.localStorage && !this._authSync) {
                    this._authSync = true;
                    try {
                        saveAuthToken(data ? data.token : undefined);
                        window.localStorage.setItem(authSyncKey, JSON.stringify(data ? data : null));
                        window.localStorage.removeItem(authSyncKey);
                    } finally {
                        this._authSync = false;
                    }
                }
                this.setState({authenticating: false, authGuard: this._authGuard.clone()});
            };

            private _onAuthSync = (event: StorageEvent) => {
                if (!this._authSync) {
                    this._authSync = true;
                    try {
                        if (event.key !== authSyncKey || typeof event.newValue !== 'string') {
                            return;
                        }
                        const s = JSON.parse(event.newValue) as ClientAuthGuardData;
                        this._authGuard.deserialize(s);
                    } finally {
                        this._authSync = false;
                    }
                }
            };

            render() {
                return (
                    <AuthContext.Provider value={{
                        authenticating: this.state.authenticating,
                        authGuard: this.state.authGuard,
                        authMethods: {
                            login: this._authGuard.login,
                            remember: this._authGuard.remember,
                            logout: this._authGuard.logout,
                        },
                    }}>
                        <WrappedAppComponent {...this.props}/>
                    </AuthContext.Provider>
                );
            }
        }

        return hoistNonReactStatics(AppWithAuth, WrappedAppComponent, {getInitialProps: true});
    };
}

const authCookie = 'auth_token';
const authSyncKey = 'sync:auth.update';

function saveAuthToken(value: string | undefined, appCtx?: AppWithAuthContext) {
    if (typeof window !== 'undefined') {
        if (value === undefined) {
            JsCookies.remove(authCookie);
        } else {
            JsCookies.set(authCookie, value);
        }
    } else if (appCtx && appCtx.cookies) {
        appCtx.cookies.set(authCookie, value, {httpOnly: false});
    }
}

function loadAuthToken(appCtx?: AppWithAuthContext) {
    if (typeof window !== 'undefined') {
        return JsCookies.get(authCookie);
    } else if (appCtx && appCtx.cookies) {
        return appCtx.cookies.get(authCookie);
    }
    return undefined;
}

function setGlobalAuthGuard(authGuard: AuthGuard) {
    if (typeof window === 'undefined') throw new Error('Global AuthGuard is client-side only');
    (global as any).__AUTH_GUARD = authGuard;
}

export function getGlobalAuthGuard(): AuthGuard {
    if (!(global as any).__AUTH_GUARD) throw new Error('Global AuthGuard is not defined');
    return (global as any).__AUTH_GUARD;
}
