import Button from 'antd/lib/button/button';
import {WithTranslation} from 'next-i18next';
import React from 'react';
import {Link} from '../components/link';
import {pageWithTranslation} from '../i18n';
import {MainLayout} from '../layout/main-layout';

export default pageWithTranslation()(function Index({t}: WithTranslation) {
    return (
        <MainLayout>
            {t('common:hello-world')} <Link href='/'><Button type='primary' size='small'>Reload</Button></Link>
        </MainLayout>
    );
});
