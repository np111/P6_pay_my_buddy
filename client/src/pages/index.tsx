import React from 'react';
import {pageWithTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';

require('../assets/css/pages/index.scss');

export default pageWithTranslation()(function Index({t}: WithTranslation) {
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
                    <div dangerouslySetInnerHTML={{__html: 'Test<br/>'.repeat(50)}}/>
                </div>
            </section>
        </MainLayout>
    );
});
