import EventEmitter from 'wolfy87-eventemitter';
import {apiClient} from '../../api/api-client';
import {AccessDeniedError, UnhandledApiError} from '../../api/api-exception';
import {ApiResponse} from '../../api/api-response';

export interface AuthGuard {
    readonly authenticated: boolean;
    readonly token?: string;
    readonly user?: AuthUser;

    login(email: string, password: string): Promise<boolean>;

    remember(token: string): Promise<boolean>;

    logout(): Promise<void>;
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

export class ClientAuthGuard extends EventEmitter implements AuthGuard {
    token?: string;
    user?: AuthUser;

    get authenticated() {
        return !!this.user;
    }

    serialize(): SerializedAuthGuard {
        return {token: this.token, user: this.user};
    }

    update(data: SerializedAuthGuard | undefined, dontEmit?: boolean) {
        data = data || {};
        const prevData = {token: this.token};
        this.token = data.token;
        this.user = data.user;
        if (dontEmit !== false) {
            this.emit('updated', data, prevData);
        }
    }

    login(email: string, password: string): Promise<boolean> {
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
                this.update(res.result);
                return true;
            }
        });
    }

    remember(token: string): Promise<boolean> {
        return apiClient.fetch({
            url: 'auth/remember',
            authToken: token,
        }).then((res: ApiResponse) => {
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
        return apiClient.fetch({
            url: 'auth/logout',
            body: {},
        }).then((res) => {
            if (res.success === false) {
                throw new UnhandledApiError(res.error);
            }
            this.update({});
        }).catch((err) => {
            // the remote session deletion failed, but only removing the local state is enough (and secure)
            this.update({});
        });
    }
}

export class ServerAuthGuard extends ClientAuthGuard {
}
