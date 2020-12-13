import React from 'react';
import {LoginForm} from '../components/auth/login-form';
import {pageWithAuth, WithAuth} from '../components/auth/with-auth';
import {pageWithTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';
import {Card} from '../components/ui/card';

require('../assets/css/pages/login.scss');

function LoginPage({t}: WithAuth & WithTranslation) {
    return (
        <MainLayout id='login' title={t('common:page.login')}>
            <div className='container sm-t'>
                <Card className='login-card'>
                    <LoginForm/>
                </Card>
            </div>
        </MainLayout>
    );
}

export default pageWithAuth({preAuthorize: 'isAnonymous'})(pageWithTranslation()(LoginPage));
