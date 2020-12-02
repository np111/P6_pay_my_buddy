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
        const reload = opts && opts.replace ? Router.replace : Router.push;
        // hack: _h is set to force-reload in all circumstances, see https://github.com/zeit/next.js/blob/0bcd1fc39bb07f67b94238a0e867e9c3fe73a163/packages/next/next-server/lib/router/router.ts#L283
        // eslint-disable-next-line @typescript-eslint/naming-convention
        return reload.call(Router, this._getCurrentUrl(), Router.asPath, {...opts, _h: true});
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
        const push = opts && opts.replace ? Router.replace : Router.push;
        return push.call(Router, this._urlToString(href) as string, this._urlToString(as), <any>opts);
    }

    private static _getCurrentUrl() {
        return {
            pathname: Router.pathname,
            query: Router.query,
            hash: window.location.hash,
        };
    }

    private static _urlToString(url?: UrlObject) {
        return url && url.href !== null ? url.href : undefined;
    }
}
