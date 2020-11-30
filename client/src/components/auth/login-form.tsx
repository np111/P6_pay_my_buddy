import Button from 'antd/lib/button/button';
import Skeleton from 'antd/lib/skeleton/Skeleton';
import {WithTranslation} from 'next-i18next';
import React from 'react';
import {withTranslation} from '../../i18n';
import {handleAsyncError} from '../../utils/react-utils';
import {WithAuth, withAuth} from './with-auth';

export const LoginForm = withAuth()(withTranslation('common')(function ({t, authenticating, authMethods, authGuard}: WithAuth & WithTranslation) {
    const login = () => {
        return authMethods
            .login('test@test.fr', 'testtest')
            // .then()
            .catch((err) => handleAsyncError(this, err));
    };
    if (authenticating) {
        return <Skeleton loading={true}/>;
    }
    if (authGuard.authenticated) {
        return null;
    }
    return (
        <>
            <Button onClick={login}>Login</Button>
        </>
    );
}));
