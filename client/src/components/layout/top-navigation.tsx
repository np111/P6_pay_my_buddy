import Menu from 'antd/lib/menu';
import {TFunction, WithTranslation} from 'next-i18next';
import React, {useEffect, useState} from 'react';
import ReactDOM from 'react-dom';
import {AppRouter, routes} from '../../utils/routes';
import {WithAuth, withAuth} from '../auth/with-auth';
import {withTranslation} from '../i18n';
import {Link, LinkProps} from '../link';

const logo = require('../../assets/img/logo_32.png');
const logo2x = require('../../assets/img/logo_32@2x.png');

export interface TopNavigationProps {
    currentPage?: string;
    transparent?: boolean;
}

interface MenuEntry {
    id: string;
    link?: LinkProps;
    onClick?: () => void;
}

const renderMenuItems = (t: TFunction, menu: MenuEntry[]) => {
    return menu.filter(e => e !== undefined).map(({id, link, onClick}) => {
        let content = <a onClick={onClick}>{t('common:page.' + id)}</a>;
        if (link) {
            content = <Link {...link}>{content}</Link>;
        }
        return <Menu.Item key={id}>{content}</Menu.Item>;
    });
};

export const TopNavigation = withAuth()(withTranslation('common')(function ({t, authGuard, currentPage, transparent}: TopNavigationProps & WithAuth & WithTranslation) {
    const [top, setTop] = useState(true);

    useEffect(() => {
        const onScroll = () => setTop(window.scrollY < 20);
        window.addEventListener('scroll', onScroll, false);
        onScroll();
        return () => window.removeEventListener('scroll', onScroll);
    }, [setTop]);

    const index = routes.index();
    let menu;
    if (authGuard.authenticated) {
        menu = [
            currentPage !== 'index' ? {id: 'index', link: index} : undefined,
            {id: 'logout', onClick: () => authGuard.logout().then(() => AppRouter.push(index))}, // TODO: page loading animation during the whole process
        ];
    } else {
        menu = [
            currentPage !== 'index' ? {id: 'index', link: index} : undefined,
            {id: 'login', link: routes.login()},
            {id: 'register', link: routes.register()},
        ];
    }

    const render = (
        <>
            <header id='header' className={(top && transparent ? 'header-top' : undefined)}>
                <div className='container'>
                    <div className='navbar'>
                        <div className='logo'>
                            <Link {...index}>
                                <a>
                                    <img
                                        alt={t('common:logo_tag')}
                                        width='217px'
                                        height='32px'
                                        src={logo}
                                        srcSet={logo + ' 1x, ' + logo2x + ' 2x'}
                                    />
                                </a>
                            </Link>
                        </div>
                        <Menu
                            mode='horizontal'
                            selectedKeys={currentPage ? [currentPage] : []}
                        >
                            {renderMenuItems(t, menu)}
                        </Menu>
                    </div>
                </div>
            </header>
        </>
    );
    return (
        // PATCH: We use a portal to render header directly into body to fix it's width when a modal is open.
        <>
            {typeof window === 'undefined' ? render : ReactDOM.createPortal(render, document.body)}
            {transparent ? undefined : <div id='header-ph'/>}
        </>
    );
}));
