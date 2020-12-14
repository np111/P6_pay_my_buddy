import AntPopconfirm, {PopconfirmProps as AntPopconfirmProps} from 'antd/lib/popconfirm';
import 'antd/lib/popconfirm/style/index.less';
import 'antd/lib/popover/style/index.less';
import React from 'react';
import {WithTranslation, withTranslation} from '../i18n';

export type PopconfirmProps = AntPopconfirmProps;
export const Popconfirm = withTranslation('common')(function Popconfirm({t, ...props}: PopconfirmProps & WithTranslation) {
    return <AntPopconfirm okText={t('common:intl.ok')} cancelText={t('common:intl.cancel')} {...props}/>;
});
