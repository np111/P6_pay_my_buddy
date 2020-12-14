import {IncomingMessage} from 'http';
import {format, parse, UrlObject} from 'url';
import {LinkProps, Router} from '../components/i18n';
import {queryStringify} from './query-utils';

export const routes = {
    index: () => ({href: '/'}),

    login: ({to}: { to?: string } = {}) => ({
        href: '/login' + queryStringify({to}),
    }),
    register: () => ({href: '/register'}),

    summary: () => ({href: '/summary'}),
    activity: () => ({href: '/activity'}),
    contacts: ({showAdd}: { showAdd?: boolean } = {}) => ({
        href: '/contacts' + queryStringify({showAdd}),
    }),
};

export interface AppRouterOptions {
    replace?: boolean;
}

export class AppRouter {
    private static _shallow = false; // state to detect shallow requests from nprogress

    public static reload(opts?: any) {
        const reload = opts && opts.replace ? Router.replace : Router.push;
        // hack: _h is set to force-reload in all circumstances, see https://github.com/zeit/next.js/blob/0bcd1fc39bb07f67b94238a0e867e9c3fe73a163/packages/next/next-server/lib/router/router.ts#L283
        // eslint-disable-next-line @typescript-eslint/naming-convention
        return reload.call(Router, this._getCurrentUrl(), Router.asPath, {...opts, _h: true});
    }

    public static updateQuery(query: any, opts?: { goto?: 'top' }) {
        const href = this._getCurrentUrl();
        const as = parse(Router.asPath, true);
        href.query = deleteUndefined({...href.query, ...query});
        if (as) {
            as.query = deleteUndefined({...as.query, ...query});
            clearUrlCache(as);
        }
        return Router.replace(href, formatUrl(as), {shallow: AppRouter._shallow = true}).then((ret) => {
            if (opts && opts.goto === 'top') {
                window.scrollTo(0, 0);
            }
            return ret;
        });
    }

    public static push(route: LinkProps, query?: any, opts?: AppRouterOptions) {
        const href = parseUrl(route.href);
        let as = parseUrl(route.as);
        if (query) {
            if (!as) as = {...href};
            if (href) {
                href.query = {...href.query as any, ...query};
                clearUrlCache(href);
            }
        }
        const push = opts && opts.replace ? Router.replace : Router.push;
        return push.call(Router, formatUrl(href), formatUrl(as), <any>opts);
    }

    public static getPath(req?: IncomingMessage) {
        if (typeof window === 'undefined') {
            return req ? req.url : undefined;
        } else {
            return Router.asPath;
        }
    }

    public static isShallow(reset?: boolean) {
        if (AppRouter._shallow) {
            if (reset === true) AppRouter._shallow = false;
            return true;
        }
        return false;
    }

    private static _getCurrentUrl() {
        return {
            pathname: Router.pathname,
            query: Router.query,
            hash: window.location.hash,
        };
    }
}

function parseUrl(url: string | UrlObject): UrlObject {
    return typeof url === 'string' ? parse(url, true) : url;
}

function formatUrl(url: UrlObject): string {
    return url ? (url.href !== null && url.href !== undefined ? url.href : format(url)) : undefined;
}

function clearUrlCache(url: any) {
    delete url.href;
    delete url.search;
}

function deleteUndefined<T>(obj: T): T {
    for (const k in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, k) && obj[k] === undefined) {
            delete obj[k];
        }
    }
    return obj;
}
