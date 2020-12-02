import React from 'react';
import {AuthGuard} from './auth';

export interface AuthContextProps {
    authenticating: boolean;
    authGuard?: AuthGuard;
}

export const AuthContext = React.createContext<AuthContextProps>({
    authenticating: true,
});
