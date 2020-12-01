import EventEmitter from 'wolfy87-eventemitter';
import {AccessDeniedError, UnhandledApiError} from '../../api/api-exception';
import {apiFetch} from '../../api/api-fetch';
import {ApiResponse} from '../../api/api-response';

export interface AuthGuard {
    readonly authenticated: boolean;
    readonly token?: string;
    readonly user?: AuthUser;
}

export interface AuthUser {
    id: number;
    name: string;
    email: string;
}

export interface SerializedAuthGuard {
    token?: string;
    user?: AuthUser;
}

/**
 * Client-side implementation of the AuthGuard.
 * A single instance is instantiated for the browsing-session lifetime and can be updated from server serialized data.
 */
export class ClientAuthGuard extends EventEmitter implements AuthGuard {
    token?: string;
    user?: AuthUser;

    get authenticated() {
        return !!this.user;
    }

    update(s: SerializedAuthGuard | undefined, disableEmit?: boolean) {
        if (s === undefined || s === null) {
            this.token = undefined;
            this.user = undefined;
        } else {
            this.token = s.token;
            this.user = s.user;
        }
        if (disableEmit !== false) {
            this.emit('updated', s);
        }
    }

    serialize(): SerializedAuthGuard {
        return {token: this.token, user: this.user};
    }

    login(email: string, password: string): Promise<boolean> {
        return apiFetch({
            authToken: false,
            url: 'auth/login',
            body: {email, password},
        }).then((res) => {
            if (res.success == false) {
                if (res.error.code === 'INVALID_CREDENTIALS') {
                    return false;
                }
                throw new UnhandledApiError(res.error);
            } else {
                this.update(res.result);
                return true;
            }
        });
    }

    remember(token: string): Promise<boolean> {
        return apiFetch({
            url: 'auth/remember',
            authToken: token,
        }).then((res: ApiResponse<{}>) => {
            if (res.success === false) {
                throw new UnhandledApiError(res.error);
            }
            this.update({token, ...res.result});
            return true;
        }).catch((err) => {
            if (err instanceof AccessDeniedError && err.invalidToken) {
                this.update({});
                return false;
            }
            throw err;
        });
    }

    logout(): Promise<void> {
        return apiFetch({
            url: 'auth/logout',
            body: {},
        }).then((res) => {
            if (res.success == false) {
                throw new UnhandledApiError(res.error);
            }
            this.update({});
        }).catch((err) => {
            // the remote session deletion failed, but only removing the local state is enough (and secure)
            this.update({});
        });
    }
}

/**
 * Server-side implementation of the AuthGuard.
 * Currently the client is never authenticated during SSR.
 */
export class ServerAuthGuard implements AuthGuard {
    get authenticated() {
        return false;
    }

    serialize() {
        return {};
    }
}
