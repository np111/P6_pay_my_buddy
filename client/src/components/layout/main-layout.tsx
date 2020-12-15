import Head from 'next/head';
import React from 'react';
import {WithTranslation, withTranslation} from '../i18n';
import {Footer} from './footer';
import {TopNavigation, TopNavigationProps} from './top-navigation';

require('antd/lib/style/color/colorPalette.less'); // required by @ant-design/dark-theme
require('antd/lib/style/index.less'); // antd theme and core
require('antd/lib/notification/style/index.less'); // antd notification
require('@fortawesome/fontawesome-svg-core/styles.css');
require('../../assets/css/layouts/main.scss');
require('../../assets/css/layouts/nprogress.scss');

const favicon32 = require('../../assets/img/favicon_32.png');
const favicon16 = require('../../assets/img/favicon_16.png');

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
                <link rel="preconnect" href="https://fonts.gstatic.com"/>
                <link href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600&display=swap" rel="stylesheet"/>
                <title>{fullTitle === true ? title : title + ' - ' + t('common:name')}</title>
                <link rel='icon' type='image/png' href={favicon32} sizes='32x32'/>
                <link rel='icon' type='image/png' href={favicon16} sizes='16x16'/>
                <link rel='icon' type='image/x-icon' href='/favicon.ico'/>
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
