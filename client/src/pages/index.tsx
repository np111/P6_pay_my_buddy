import Button from 'antd/lib/button/button';
import {WithTranslation} from 'next-i18next';
import React from 'react';
import {LoginForm} from '../components/auth/login-form';
import {WithAuth, withAuth} from '../components/auth/with-auth';
import {MainLayout} from '../components/layout/main-layout';
import {Link} from '../components/link';
import {pageWithTranslation} from '../i18n';

export default withAuth()(pageWithTranslation()(function Index({t, authMethods, authGuard}: WithAuth & WithTranslation) {
    return (
        <MainLayout>
            <p>
                {t('common:hello-world')} <Link href='/'><Button type='primary' size='small'>Reload</Button></Link>
            </p>
            {!authGuard.authenticated ? (
                <LoginForm/>
            ) : (
                <>
                    Hello #{authGuard.user.id} - {authGuard.user.name}
                    <Button onClick={authMethods.logout}>Logout</Button>
                </>
            )}
        </MainLayout>
    );
}));
