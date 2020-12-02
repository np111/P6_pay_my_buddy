import {default as _fetch} from 'isomorphic-unfetch';
import getConfig from 'next/config';
import {Router} from 'next/router';
import {getGlobalAuthGuard, NextPageWithAuthContext} from '../components/auth/app-with-auth';
import {AccessDeniedError, ApiException} from './api-exception';
import {ApiResponse} from './api-response';

export interface ApiFetchRequest {
    ctx?: NextPageWithAuthContext; // SSR page context
    authToken?: string | false;
    method?: 'GET' | 'POST';
    url: string;
    body?: any;
    neverCancel?: boolean;
}

export class ApiClient {
    private readonly _baseUrl: string;
    private readonly _abortSignal?: () => AbortSignal | undefined;

    constructor(baseUrl: string, abortSignal?: () => AbortSignal | undefined) {
        this._baseUrl = baseUrl;
        this._abortSignal = abortSignal;
    }

    fetch<T = any>({ctx, authToken, method, url, body, neverCancel}: ApiFetchRequest): Promise<ApiResponse<T>> {
        const headers: any = {};

        // Resolve auth token
        if (authToken !== false && authToken === undefined) {
            if (typeof window !== 'undefined') {
                authToken = getGlobalAuthGuard().token;
            } else {
                if (!ctx) {
                    throw new ApiException('ctx is missing in SSR context');
                }
                authToken = ctx.authGuard.token;
            }
        }
        if (authToken === false || authToken === undefined) {
            authToken = 'anonymous';
        }
        headers['X-Auth-Token'] = authToken;

        // Serialize body (and auto-detect method)
        if (body) {
            method = method || 'POST';
            body = JSON.stringify(body);
            headers['Content-Type'] = 'application/json;charset=utf-8';
        }
        method = method || 'GET';

        // Fetch
        const input: RequestInfo = this._baseUrl + url;
        const init: RequestInit = {method, headers, body};
        if (neverCancel !== true && this._abortSignal) {
            init.signal = this._abortSignal();
        }
        return _fetch(input, init).then((httpRes): Promise<ApiResponse<T>> => {
            const statusClass = Math.floor(httpRes.status / 100);
            if (statusClass === 2) {
                return this._parseJson(httpRes).then((result) => ({success: true, result}));
            } else if (statusClass === 4) {
                return this._parseJson(httpRes).then((error) => {
                    if (typeof error === 'object' && error && typeof error.code === 'string' && typeof error.message === 'string') {
                        if (error && error.type !== 'SERVICE') {
                            switch (error.code) {
                                case 'SERVER_EXCEPTION':
                                    throw new ApiException('Server-side exception (' + error.code + ')', error);
                                case 'ACCESS_DENIED':
                                    throw new AccessDeniedError(error);
                                default:
                                    throw new ApiException('Client-side exception (' + error.code + ')', error);
                            }
                        }
                        return {success: false, error};
                    }
                    throw new ApiException('Unsupported server response: incomplete error fields');
                });
            } else if (statusClass === 5) {
                throw new ApiException('Server exception: HTTP ' + httpRes.status);
            } else {
                throw new ApiException('Unsupported server response: HTTP ' + httpRes.status);
            }
        }).catch((e: any): ApiResponse<T> => {
            if (e && e.name === 'AbortError') {
                return {success: false, error: {type: 'CLIENT', code: '_ABORTED', message: 'Request was aborted by the client'}};
            }
            throw e;
        });
    }

    private _parseJson(httpRes: Response) {
        switch (httpRes.status) {
            default:
                return httpRes.json().catch((err) => {
                    throw new ApiException('Unsupported server response: invalid json', err);
                });
            case 204:
                return Promise.resolve(undefined);
            case 279:
                window.location.reload();
                return new Promise<any>((ignored) => {
                    // never completing promise, waiting for the page to reload
                });
        }
    }
}

export const apiClient = (() => {
    const {serverRuntimeConfig, publicRuntimeConfig} = getConfig();
    const apiUrl = serverRuntimeConfig.apiUrl || publicRuntimeConfig.apiUrl;
    let abortSignal;
    if (typeof window !== 'undefined' && typeof AbortController !== 'undefined') {
        let signal: AbortSignal;
        let controller: AbortController;
        const initController = () => {
            controller = new AbortController();
            signal = controller.signal;
        };
        const abortController = () => {
            controller.abort();
            initController();
        };
        initController();
        Router.events.on('routeChangeStart', abortController);
        abortSignal = () => signal;
    }
    return new ApiClient(apiUrl, abortSignal);
})();
