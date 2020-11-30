import EventEmitter from 'wolfy87-eventemitter';

export interface AuthGuard {
    readonly authenticated: boolean;
    readonly token?: string;
    readonly user?: AuthUser;

    serialize(): SerializedAuthGuard;
}

export interface AuthUser {
    id: number;
    email: string;
    name: string;
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

    serialize() {
        return {token: this.token, user: this.user};
    }
}

/**
 * Server-side implementation of the AuthGuard.
 * Currently the client is never authenticated in SSR.
 */
export class ServerAuthGuard implements AuthGuard {
    get authenticated() {
        return false;
    }

    serialize() {
        return {};
    }
}

export interface ClientAuthMethods {
    login(email: string, password: string): Promise<boolean>;

    logout(): Promise<void>;
}
