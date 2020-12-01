import Head from 'next/head';
import React from 'react';
import {WithTranslation, withTranslation} from '../i18n';
import {Footer} from './footer';
import {TopNavigation, TopNavigationProps} from './top-navigation';

require('antd/lib/style/color/colorPalette.less'); // required by @ant-design/dark-theme
require('antd/lib/style/index.less'); // antd theme and core
require('antd/lib/button/style/index.less');
require('antd/lib/form/style/index.less');
require('antd/lib/grid/style/index.less');
require('antd/lib/input/style/index.less');
require('antd/lib/menu/style/index.less');
require('antd/lib/skeleton/style/index.less');
require('antd/lib/spin/style/index.less');

require('../../assets/css/app.scss');

export interface MainLayoutProps {
    id?: string;
    section?: string;
    title?: string;
    fullTitle?: boolean;
    topNavigation?: TopNavigationProps;
    head?: React.ReactNode;
    children: React.ReactNode;
}

export const MainLayout = withTranslation()(function ({t, id, section, title, fullTitle, head, topNavigation, children}: WithTranslation & MainLayoutProps) {
    return (
        <>
            <Head>
                <meta name='viewport' content='width=device-width, initial-scale=1'/>
                <title>{fullTitle === true ? title : title + ' - ' + t('common:name')}</title>
                {head}
            </Head>
            <div className='page'>
                <TopNavigation currentPage={section || id} {...topNavigation}/>
                <main id='main'>
                    {children}
                </main>
                <Footer/>
            </div>
        </>
    );
});
