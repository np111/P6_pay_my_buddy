import hoistNonReactStatics from 'hoist-non-react-statics';
import {IncomingMessage} from 'http';
import React, {useContext, useEffect} from 'react';
import {AppRouter, routes} from '../../utils/routes';
import {LinkProps} from '../i18n';
import {MainLayout} from '../layout/main-layout';
import {Skeleton} from '../ui/skeleton';
import {NextPageWithAuthContext} from './app-with-auth';
import {AuthGuard, AuthMethods} from './auth';
import {AuthContext} from './auth-context';

export interface WithAuth {
    authenticating: boolean;
    authGuard: AuthGuard;
    authMethods: AuthMethods;
}

export interface PageWithAuthOpts {
    preAuthorize?: 'isAuthenticated' | 'isAnonymous';
}

export function pageWithAuth({preAuthorize}: PageWithAuthOpts = {}) {
    return <P extends WithAuth>(WrappedComponent: React.ComponentType<P> & { getInitialProps?: any }) => {
        let usedAuthCtx;

        function PageWithAuthHOC(props: Omit<P, keyof WithAuth>) {
            const authCtx = useContext(AuthContext);

            const actions = checkPreAuthorizeActions(authCtx.authenticating, authCtx.authGuard);
            const route = actions ? actions.route : undefined;
            const allowed = actions === undefined;

            useEffect(() => {
                if (route !== undefined) {
                    return AppRouter.push(route);
                }
            }, [route]);

            if (allowed) {
                usedAuthCtx = authCtx;
            }

            if (!usedAuthCtx) {
                return (
                    <MainLayout id='loading' title='...'>
                        <div className='container'>
                            <Skeleton/>
                        </div>
                    </MainLayout>
                );
            }
            return (
                <AuthContext.Provider value={usedAuthCtx}>
                    <WrappedComponent {...props} {...usedAuthCtx}/>
                </AuthContext.Provider>
            );
        }

        PageWithAuthHOC.getInitialProps = (ctx: NextPageWithAuthContext) => {
            if (typeof window === 'undefined') {
                const actions = checkPreAuthorizeActions(ctx.authenticating, ctx.authGuard, ctx.req);
                if (actions && actions.route) {
                    // TODO: SSR redirection
                }
            }
            return WrappedComponent.getInitialProps ? WrappedComponent.getInitialProps(ctx) : {};
        };

        PageWithAuthHOC.isPageWithAuth = true;

        function checkPreAuthorizeActions(authenticating: boolean, authGuard: AuthGuard, req?: IncomingMessage): { route?: LinkProps } | undefined {
            if (authenticating || !authGuard) {
                return {};
            }
            if (preAuthorize === 'isAuthenticated' && !authGuard.authenticated) {
                return {route: routes.login({to: AppRouter.getPath(req)})};
            }
            if (preAuthorize === 'isAnonymous' && authGuard.authenticated) {
                return {route: routes.summary()};
            }
            return undefined;
        }

        return hoistNonReactStatics(PageWithAuthHOC, WrappedComponent, {getInitialProps: true});
    };
}

export function withAuth() {
    return <P extends WithAuth>(WrappedComponent: React.ComponentType<P>) => {
        function WithAuthHOC(props: Omit<P, keyof WithAuth>) {
            const authCtx = useContext(AuthContext);
            // @ts-ignore
            return <WrappedComponent {...props} {...authCtx}/>;
        }

        return hoistNonReactStatics(WithAuthHOC, WrappedComponent);
    };
}
