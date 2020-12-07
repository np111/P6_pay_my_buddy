import React from 'react';
import {pageWithAuth, WithAuth} from '../components/auth/with-auth';
import {pageWithTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';

require('../assets/css/pages/summary.scss');

export default pageWithAuth({preAuthorize: 'isAuthenticated'})(pageWithTranslation()(function Index({t, authGuard}: WithAuth & WithTranslation) {
    return (
        <MainLayout
            id='summary'
        >
            <div className='container'>
                <section>
                    Bonjour {authGuard.user.name},
                </section>
            </div>
        </MainLayout>
    );
}));
