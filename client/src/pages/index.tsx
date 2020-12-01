import React from 'react';
import {WithAuth, withAuth} from '../components/auth/with-auth';
import {pageWithTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';

export default withAuth()(pageWithTranslation()(function Index({t, authMethods, authGuard}: WithAuth & WithTranslation) {
    return (
        <MainLayout
            id='index'
            title={t('common:tag') + ' | ' + t('common:name')}
            fullTitle={true}
            topNavigation={{transparent: true}}
        >
            {/* TODO */}
        </MainLayout>
    );
}));
