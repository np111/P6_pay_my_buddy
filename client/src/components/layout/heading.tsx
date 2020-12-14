import classNames from 'classnames';
import React from 'react';
import '../../assets/css/layouts/heading.scss';
import {AppRouter, routes} from '../../utils/routes';
import {Link, Trans} from '../i18n';
import {Button} from '../ui/button';
import {Icon} from '../ui/icon';
import {iconAddContact} from '../ui/icons/icon-addcontact';
import {iconSendMoney} from '../ui/icons/icon-sendmoney';

export type HeadingProps = {
    title?: string;
    actions?: React.ReactElement[];
    onAddContact?: () => void;
} & Omit<React.HTMLAttributes<any>, 'children' | 'title'>;

export function Heading({title, actions, onAddContact, ...divProps}: HeadingProps) {
    if (actions === undefined) {
        actions = [
            <Link key='send-money' {...routes.sendMoney()}>
                <Button type='primary' block={true}>
                    <Icon {...iconSendMoney} marginRight={true}/><Trans i18nKey='common:actions.send_money'/>
                </Button>
            </Link>,
            <Button key='add-contact' type='default' block={true} onClick={onAddContact || defaultOnAddContact}>
                <Icon {...iconAddContact} marginRight={true}/><Trans i18nKey='common:actions.add_contact'/>
            </Button>,
        ];
    }
    return (
        <div {...divProps} className={classNames('heading', divProps.className)}>
            <h1>{title}</h1>
            {!actions ? undefined : (
                <div className='actions'>{actions.map(renderAction)}</div>
            )}
        </div>
    );
}

function defaultOnAddContact() {
    return AppRouter.push(routes.contacts({showAdd: true}));
}

export function renderAction(action: React.ReactElement) {
    return <div className='action' key={action.key}>{action}</div>;
}
