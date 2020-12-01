import React from 'react';
import {LoginForm} from '../components/auth/login-form';
import {WithAuth, withAuth} from '../components/auth/with-auth';
import {pageWithTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';
import {routes} from '../utils/routes';

require('../assets/css/pages/login.scss');

export default withAuth()(pageWithTranslation()(function Login({t, authGuard}: WithAuth & WithTranslation) {
    return (
        <MainLayout id='login' title={t('common:page.login')}>
            <div className='container sm-t'>
                {!authGuard.authenticated ? (
                    <LoginForm redirect={routes.index()}/>
                ) : (
                    <>TODO: Redirect to account</>
                )}
            </div>
        </MainLayout>
    );
}));
