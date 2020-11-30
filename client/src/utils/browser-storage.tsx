export class BrowserStorage {
    private readonly _handle: Storage;

    constructor(handle: Storage) {
        this._handle = handle;
    }

    set(key: string, value: any): boolean {
        if (this._handle) {
            if (value === undefined) {
                this._handle.removeItem(key);
            } else {
                this._handle.setItem(key, JSON.stringify(value));
            }
            return true;
        }
        return false;
    }

    get(key: string, def?: any): any {
        if (this._handle) {
            const strData = this._handle.getItem(key);
            try {
                return JSON.parse(strData);
            } catch (ignored) {
            }
        }
        return def;
    }

    getChecked(key: string, check: string | ((data: any) => boolean), def?: any): any {
        const data = this.get(key);
        if (data !== undefined && (typeof check === 'function' ? check(data) : typeof data === check)) {
            return data;
        }
        return def;
    }

    remove(key: string): void {
        if (this._handle) {
            this._handle.removeItem(key);
        }
    }
}

export const browserLocalStorage = new BrowserStorage(
    // note: try to fallback on sessionStorage if localStorage is undefined (this happens with some browsers incognito mode)
    typeof window !== 'undefined' ? window.localStorage || window.sessionStorage : undefined);

export const browserSessionStorage = new BrowserStorage(
    typeof window !== 'undefined' ? window.sessionStorage : undefined);
