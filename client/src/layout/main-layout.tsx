import Head from 'next/head';
import React from 'react';

require('antd/lib/style/color/colorPalette.less'); // required by @ant-design/dark-theme
require('antd/lib/style/index.less'); // antd theme and core
require('antd/lib/button/style/index.less');

require('../assets/css/app.scss');

export interface MainLayoutProps {
    children: React.ReactNode;
}

export function MainLayout({children}: MainLayoutProps) {
    return (
        <>
            <Head>
                <meta name='viewport' content='width=device-width, initial-scale=1'/>
            </Head>
            {children}
        </>
    );
}
