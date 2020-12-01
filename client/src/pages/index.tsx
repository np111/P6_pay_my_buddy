import React from 'react';
import {WithAuth, withAuth} from '../components/auth/with-auth';
import {pageWithTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';

require('../assets/css/pages/index.scss');

export default withAuth()(pageWithTranslation()(function Index({t, authGuard}: WithAuth & WithTranslation) {
    return (
        <MainLayout
            id='index'
            title={t('common:tag') + ' | ' + t('common:name')}
            fullTitle={true}
            topNavigation={{transparent: true}}
        >
            <section className='home-hero'>
                {/* TODO */}
            </section>
            <section>
                <div className='container sm-t'>
                    {/* TODO */}
                    {authGuard.authenticated ? 'Hello ' + authGuard.user.name + ' ! (' + authGuard.user.email + ')' : null}
                    <div dangerouslySetInnerHTML={{__html: 'Test<br/>'.repeat(50)}}/>
                </div>
            </section>
        </MainLayout>
    );
}));
