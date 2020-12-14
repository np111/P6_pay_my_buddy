import EventEmitter from 'wolfy87-eventemitter';
import {apiClient} from '../../api/api-client';
import {AccessDeniedError, UnhandledApiError} from '../../api/api-exception';
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
    defaultCurrency: string;
}

export interface AuthMethods {
    readonly login: (email: string, password: string) => Promise<boolean>;
    readonly remember: (token: string) => Promise<boolean>;
    readonly logout: () => Promise<void>;
}

export interface ClientAuthGuardData {
    token?: string;
    user?: AuthUser;
}

export interface UpdateAuthContext {
    noEmit?: boolean;
}

export class ClientAuthGuard extends EventEmitter implements AuthGuard, AuthMethods {
    private _data: ClientAuthGuardData = {};

    get authenticated() {
        return !!this.user;
    }

    get token(): string | undefined {
        return this._data.token;
    }

    get user(): AuthUser | undefined {
        return this._data.user;
    }

    serialize = (): ClientAuthGuardData => {
        return {token: this.token, user: this.user ? {...this.user} : undefined};
    };

    deserialize = (data: ClientAuthGuardData | undefined, ctx: UpdateAuthContext = {}) => {
        const prevData = this._data;
        this._data = data || {};
        if (!ctx.noEmit) {
            this.emit('updated', this._data, prevData, ctx);
        }
    };

    clone(): AuthGuard {
        return {
            authenticated: this.authenticated,
            token: this.token,
            user: this.user ? {...this.user} : undefined,
        };
    }

    login = (email: string, password: string): Promise<boolean> => {
        return apiClient.fetch({
            authToken: false,
            url: 'auth/login',
            body: {email, password},
        }).then((res) => {
            if (res.success === false) {
                if (res.error.code === 'INVALID_CREDENTIALS') {
                    return false;
                }
                throw new UnhandledApiError(res.error);
            } else {
                this.deserialize(res.result);
                return true;
            }
        });
    };

    remember = (token: string): Promise<boolean> => {
        return apiClient.fetch({
            authToken: token,
            url: 'auth/remember',
        }).then((res: ApiResponse) => {
            if (res.success === false) {
                throw new UnhandledApiError(res.error);
            }
            this.deserialize({token, ...res.result});
            return true;
        }).catch((err) => {
            if (err instanceof AccessDeniedError && err.invalidToken) {
                this.deserialize({});
                return false;
            }
            throw err;
        });
    };

    logout = (): Promise<void> => {
        return apiClient.fetch({
            authToken: this._data.token,
            url: 'auth/logout',
            body: {},
        }).then((res) => {
            if (res.success === false) {
                throw new UnhandledApiError(res.error);
            }
            this.deserialize({});
        }).catch((err) => {
            // the remote session deletion failed, but only removing the local state is enough (and secure)
            this.deserialize({});
        });
    };
}

export class ServerAuthGuard extends ClientAuthGuard {
}
