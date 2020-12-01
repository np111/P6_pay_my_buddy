import {LinkProps} from 'next/link';
import {parse, UrlObject} from 'url';
import {Router} from '../components/i18n';

export const routes = {
    index: () => ({href: '/'}),

    login: () => ({href: '/login'}),
    register: () => ({href: '/register'}),
};

export interface AppRouterOptions {
    replace?: boolean;
}

function toString(url?: UrlObject) {
    if (url && url.href !== null) {
        return url.href;
    }
    return undefined;
}

export class AppRouter {
    public static push(route: LinkProps, query?: any, opts?: AppRouterOptions) {
        const href = typeof route.href === 'string' ? parse(route.href, true) : route.href;
        let as = typeof route.as === 'string' ? parse(route.as, true) : route.as;
        if (query) {
            if (!as) as = {...href};
            if (href) {
                href.query = {...href.query as any, ...query};
                delete href.search;
            }
        }
        return opts && opts.replace === true
            ? Router.replace(toString(href) as string, toString(as), <any>opts)
            : Router.push(toString(href) as string, toString(as), <any>opts);
    }
}
