import Head from 'next/head';
import React from 'react';
import {WithTranslation, withTranslation} from '../i18n';
import {Footer} from './footer';
import {TopNavigation, TopNavigationProps} from './top-navigation';

require('antd/lib/style/color/colorPalette.less'); // required by @ant-design/dark-theme
require('antd/lib/style/index.less'); // antd theme and core
require('../../assets/css/layouts/common.scss');
require('../../assets/css/layouts/nprogress.scss');

export interface MainLayoutProps {
    id?: string;
    section?: string;
    title?: string;
    fullTitle?: boolean;
    head?: React.ReactNode;
    topNavigation?: TopNavigationProps;
    children?: React.ReactNode;
}

export const MainLayout = withTranslation()(function ({t, id, section, title, fullTitle, head, topNavigation, children}: WithTranslation & MainLayoutProps) {
    return (
        <>
            <Head>
                <meta name='viewport' content='width=device-width, initial-scale=1'/>
                <title>{fullTitle === true ? title : title + ' - ' + t('common:name')}</title>
                {head}
            </Head>
            <div id={id} className='page'>
                <TopNavigation currentPage={section || id} {...topNavigation}/>
                <main id='main'>
                    {children}
                </main>
                <Footer/>
            </div>
        </>
    );
});
