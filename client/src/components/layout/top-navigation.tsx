import {TFunction, WithTranslation} from 'next-i18next';
import React, {useEffect, useState} from 'react';
import ReactDOM from 'react-dom';
import {withNProgress} from '../../utils/react-utils';
import {routes} from '../../utils/routes';
import {WithAuth, withAuth} from '../auth/with-auth';
import {Link, LinkProps, withTranslation} from '../i18n';
import {Menu} from '../ui/menu';
import {Skeleton} from '../ui/skeleton';

require('../../assets/css/layouts/header.scss');
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
    return menu.filter((e) => e !== undefined).map(({id, link, onClick}) => {
        let content = <a onClick={onClick}>{t('common:page.' + id)}</a>;
        if (link) {
            content = <Link {...link}>{content}</Link>;
        }
        return <Menu.Item key={id}>{content}</Menu.Item>;
    });
};

export const TopNavigation = withAuth()(withTranslation('common')(function ({t, authGuard, authMethods, currentPage, transparent}: TopNavigationProps & WithAuth & WithTranslation) {
    const [top, setTop] = useState(true);

    useEffect(() => {
        const onScroll = () => setTop(window.scrollY < 20);
        window.addEventListener('scroll', onScroll, false);
        onScroll();
        return () => window.removeEventListener('scroll', onScroll);
    }, [setTop]);

    let home;
    let menu;
    if (authGuard.authenticated) {
        home = routes.summary();
        menu = [
            {id: 'summary', link: routes.summary()},
            {id: 'contacts', link: routes.contacts()},
            {id: 'logout', onClick: () => withNProgress(authMethods.logout())},
        ];
    } else {
        home = routes.index();
        menu = [
            currentPage !== 'index' ? {id: 'index', link: routes.index()} : undefined,
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
                            <Link {...home}>
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
                        {currentPage === 'loading' ? (
                            <div className='skeleton-menu'>
                                <Skeleton.Button/>
                                <Skeleton.Button/>
                                <Skeleton.Button/>
                            </div>
                        ) : (
                            <Menu
                                mode='horizontal'
                                selectedKeys={currentPage ? [currentPage] : []}
                            >
                                {renderMenuItems(t, menu)}
                            </Menu>
                        )}
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
