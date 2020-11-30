import hoistNonReactStatics from 'hoist-non-react-statics';
import React from 'react';
import {ClientAuthGuard, ClientAuthMethods} from './auth';
import {AuthContext} from './auth-context';

export interface WithAuth {
    authenticating: boolean;
    authMethods: ClientAuthMethods;
    authGuard: ClientAuthGuard;
}

export function withAuth() { // wrap everything into a function in the case we add an options param
    return <P extends WithAuth>(WrappedComponent: React.ComponentType<P>) => {
        class WithAuthHOC extends React.Component<Omit<P, keyof WithAuth>> {
            render() {
                return (
                    <AuthContext.Consumer>{(authCtx) => {
                        return (
                            // @ts-ignore
                            <WrappedComponent
                                {...this.props}
                                authenticating={authCtx.authenticating}
                                authMethods={authCtx.authMethods}
                                authGuard={authCtx.authGuard}
                            />
                        );
                    }}</AuthContext.Consumer>
                );
            }
        }

        return hoistNonReactStatics(WithAuthHOC, WrappedComponent);
    };
}
