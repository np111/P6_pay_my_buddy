import {parse, UrlObject} from 'url';
import {LinkProps, Router} from '../components/i18n';

export const routes = {
    index: () => ({href: '/'}),

    login: () => ({href: '/login'}),
    register: () => ({href: '/register'}),
};

export interface AppRouterOptions {
    replace?: boolean;
}

export class AppRouter {
    public static reload(opts?: any) {
        // hack: _h is set to force-reload in all circumstances
        // https://github.com/zeit/next.js/blob/0bcd1fc39bb07f67b94238a0e867e9c3fe73a163/packages/next/next-server/lib/router/router.ts#L283
        return opts && opts.replace === true
            ? Router.replace(this.getCurrentUrl(), Router.asPath, {...opts, _h: true})
            : Router.push(this.getCurrentUrl(), Router.asPath, {...opts, _h: true});
    }

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
            ? Router.replace(this.urlToString(href) as string, this.urlToString(as), <any>opts)
            : Router.push(this.urlToString(href) as string, this.urlToString(as), <any>opts);
    }

    private static getCurrentUrl() {
        return {
            pathname: Router.pathname,
            query: Router.query,
            hash: window.location.hash,
        };
    }

    private static urlToString(url?: UrlObject) {
        return url && url.href !== null ? url.href : undefined;
    }
}
