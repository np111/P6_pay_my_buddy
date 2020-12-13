import AntdModal, {ModalProps as AntModalProps} from 'antd/lib/modal';
import 'antd/lib/modal/style/index.less';
import React from 'react';
import {Icon} from './icon';
import {iconClose} from './icons/icon-close';

export type ModalProps = AntModalProps & {
    children?: React.ReactNode;
};

export function Modal(props: ModalProps) {
    return <AntdModal closeIcon={<Icon {...iconClose}/>} footer={null} {...props}/>;
}
