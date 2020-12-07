import React from 'react';
import {AuthGuard, AuthMethods} from './auth';

export interface AuthContextProps {
    readonly authenticating: boolean;
    readonly authGuard?: AuthGuard;
    readonly authMethods: AuthMethods;
}

export const AuthContext = React.createContext<AuthContextProps>({
    authenticating: true,
    authMethods: {
        login: () => Promise.resolve(false),
        remember: () => Promise.resolve(false),
        logout: () => Promise.resolve(),
    },
});
