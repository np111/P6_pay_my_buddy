import React from 'react';
import {ClientAuthGuard} from './auth';

export interface AuthContextProps {
    authenticating: boolean;
    authGuard?: ClientAuthGuard;
}

export const AuthContext = React.createContext<AuthContextProps>({
    authenticating: true,
});
