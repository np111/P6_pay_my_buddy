import React from 'react';
import {ClientAuthGuard, ClientAuthMethods} from './auth';

export interface AuthContextProps {
    authenticating: boolean;
    authMethods: ClientAuthMethods;
    authGuard?: ClientAuthGuard;
}

export const AuthContext = React.createContext<AuthContextProps>({
    authenticating: true,
    authMethods: {login: () => Promise.resolve(false), logout: () => Promise.resolve()},
});
