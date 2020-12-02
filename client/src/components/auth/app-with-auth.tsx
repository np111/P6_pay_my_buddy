import hoistNonReactStatics from 'hoist-non-react-statics';
import JsCookies from 'js-cookie';
import {AppContext} from 'next/app';
import {WithRouterProps} from 'next/dist/client/with-router';
import {NextPageContext} from 'next/dist/next-server/lib/utils';
import {withRouter} from 'next/router';
import React from 'react';
import {catchAsyncError} from '../../utils/react-utils';
import {AppRouter} from '../../utils/routes';
import {ClientAuthGuard, SerializedAuthGuard, ServerAuthGuard} from './auth';
import {AuthContext} from './auth-context';

export interface AppWithAuthContext extends AppContext {
    ctx: NextPageWithAuthContext;
    cookies?: { get(name: string): void, set(name: string, value?: string, options?: any): void }
}

export interface NextPageWithAuthContext extends NextPageContext {
    authGuard: ClientAuthGuard;
}

interface AppWithAuthProps {
    authGuard: SerializedAuthGuard;
}

export function appWithAuth() { // wrap everything into a function in the case we add an options param
    const initSsrCookiesIfNeeded = (appCtx: AppWithAuthContext) => {
        if (typeof window === 'undefined' && !appCtx.cookies) {
            appCtx.cookies = new (require('cookies'))(appCtx.ctx.req, appCtx.ctx.res);
        }
    };
    return (WrappedAppComponent) => {
        // noinspection JSPotentiallyInvalidUsageOfThis
        class AppWithAuth extends React.Component<AppWithAuthProps & WithRouterProps> {
            static async getInitialProps(appCtx: AppWithAuthContext) {
                const {req} = appCtx.ctx;
                let ssrAuthGuard;
                if (req) {
                    ssrAuthGuard = new ServerAuthGuard();
                    if (true) { // TODO: add an option to enable/disable SSR authentication
                        initSsrCookiesIfNeeded(appCtx);
                        const token = loadAuthToken(appCtx);
                        if (token && !await ssrAuthGuard.remember(token)) {
                            saveAuthToken(undefined, appCtx);
                        }
                    }
                    appCtx.ctx.authGuard = ssrAuthGuard;
                } else {
                    appCtx.ctx.authGuard = getGlobalAuthGuard();
                }

                let wrappedComponentProps;
                if (WrappedAppComponent.getInitialProps) {
                    wrappedComponentProps = await WrappedAppComponent.getInitialProps(appCtx);
                }

                return {
                    authGuard: ssrAuthGuard ? ssrAuthGuard.serialize() : undefined,
                    ...wrappedComponentProps,
                };
            }

            authSync = false;
            authStore = {
                authenticating: false,
                authGuard: new ClientAuthGuard(),
            };

            constructor(props: AppWithAuthProps & WithRouterProps, ctx: any) {
                super(props, ctx);
                this.authStore.authGuard.update(props.authGuard, true);
                if (typeof window !== 'undefined') {
                    setGlobalAuthGuard(this.authStore.authGuard);
                    this.authStore.authenticating = this.authStore.authGuard.token !== loadAuthToken();
                }
            }

            componentDidMount() {
                this.authStore.authGuard.addListener('updated', this.onAuthUpdated);
                window.addEventListener('storage', this.onAuthSync);

                const remember = (): Promise<any> => {
                    const token = loadAuthToken();
                    if (this.authStore.authGuard.token !== token) {
                        if (token) {
                            // Not remembered in SSR, do client-side remembering
                            return this.authStore.authGuard.remember(token).catch((err) => catchAsyncError(this, err));
                        } else {
                            // Remembered in SSR, but disconnected since
                            this.authStore.authGuard.update({});
                        }
                    }
                    return Promise.resolve();
                };
                remember().finally(() => {
                    if (this.authStore.authenticating) {
                        this.authStore.authenticating = false;
                        this.forceUpdate();
                    }
                });
            }

            componentWillUnmount() {
                window.removeEventListener('storage', this.onAuthSync);
                this.authStore.authGuard.removeListener('updated', this.onAuthUpdated);
            }

            onAuthUpdated = (data: SerializedAuthGuard, prevData: SerializedAuthGuard) => {
                this.authStore.authenticating = false;
                if (window.localStorage && !this.authSync) {
                    this.authSync = true;
                    try {
                        saveAuthToken(data ? data.token : undefined);
                        window.localStorage.setItem(authSyncKey, JSON.stringify(data ? data : null));
                        window.localStorage.removeItem(authSyncKey);
                    } finally {
                        this.authSync = false;
                    }
                }
                if (prevData.token === this.authStore.authGuard.token) {
                    this.forceUpdate();
                } else {
                    // noinspection JSIgnoredPromiseFromCall
                    AppRouter.reload({replace: true});
                }
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

            render() {
                return (
                    <AuthContext.Provider value={this.authStore}>
                        <WrappedAppComponent {...this.props}/>
                    </AuthContext.Provider>
                );
            }
        }

        return hoistNonReactStatics(withRouter(AppWithAuth), WrappedAppComponent, {getInitialProps: true});
    };
}

const authCookie = 'auth_token';
const authSyncKey = 'sync:auth.update';

function saveAuthToken(value: string | undefined, appCtx?: AppWithAuthContext) {
    // TODO: set max age or session-only, if needed
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

function setGlobalAuthGuard(authGuard: ClientAuthGuard) {
    if (typeof window === 'undefined') throw new Error('Global AuthGuard is client-side only');
    (global as any).__AUTH_GUARD = authGuard;
}

export function getGlobalAuthGuard(): ClientAuthGuard {
    if (!(global as any).__AUTH_GUARD) throw new Error('Global AuthGuard is not defined');
    return (global as any).__AUTH_GUARD;
}
