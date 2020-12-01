import Menu from 'antd/lib/menu';
import {TFunction, WithTranslation} from 'next-i18next';
import React, {useEffect, useState} from 'react';
import ReactDOM from 'react-dom';
import {routes} from '../../utils/routes';
import {withTranslation} from '../i18n';
import {Link} from '../link';

const logo = '';
const logo2x = '';

export interface TopNavigationProps {
    currentPage?: string;
    transparent?: boolean;
}

const index = routes.index();
const menus = [
    {id: 'index', link: index},
    {id: 'login', link: routes.login()},
    {id: 'register', link: routes.register()},
];

const renderMenuItems = (t: TFunction) => {
    return menus.map((menu) => (
        <Menu.Item key={menu.id}>
            <Link {...menu.link}>
                <a>
                    {t('common:page.' + menu.id)}
                </a>
            </Link>
        </Menu.Item>
    ));
};

export const TopNavigation = withTranslation('common')(function ({t, currentPage, transparent}: TopNavigationProps & WithTranslation) {
    const [top, setTop] = useState(true);

    useEffect(() => {
        const onScroll = () => setTop(window.scrollY < 20);
        window.addEventListener('scroll', onScroll, false);
        onScroll();
        return () => window.removeEventListener('scroll', onScroll);
    }, [setTop]);

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
                                        width='144px'
                                        height='48px'
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
                            {renderMenuItems(t)}
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
});
