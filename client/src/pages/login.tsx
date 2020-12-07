import React from 'react';
import {LoginForm} from '../components/auth/login-form';
import {pageWithAuth, WithAuth} from '../components/auth/with-auth';
import {pageWithTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';

require('../assets/css/pages/login.scss');

export default pageWithAuth({preAuthorize: 'isAnonymous'})(pageWithTranslation()(function Login({t}: WithAuth & WithTranslation) {
    return (
        <MainLayout id='login' title={t('common:page.login')}>
            <div className='container sm-t'>
                <LoginForm/>
            </div>
        </MainLayout>
    );
}));
