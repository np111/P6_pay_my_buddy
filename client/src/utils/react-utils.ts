import NProgress from 'nprogress';
import React, {useRef, useState} from 'react';

export function catchAsyncError(component: React.Component, err: any) {
    component.setState(() => {
        /*
        if (component.componentDidCatch) {
            component.componentDidCatch(err, undefined);
            return;
        }
        */
        throw err;
    });
}

export function useCatchAsyncError() {
    const [/* state */, setState] = useState();
    return (err) => {
        setState(() => {
            throw err;
        });
    };
}

export function useStickyResult<T>(value: T): T {
    const val = useRef<T>();
    if (value !== undefined) {
        val.current = value;
    }
    return val.current;
}

export function noop() {
    return Promise.resolve();
}

// eslint-disable-next-line
export function doBindArgs<T extends Function>(fn: T, ...args: any[]): (...args: any[]) => void {
    return fn.bind.apply(fn, [undefined, ...args]);
}

export function withNProgress<T>(promise: Promise<T>): Promise<T> {
    if (typeof window !== 'undefined') {
        NProgress.start();
        promise.then(() => NProgress.done()).catch(() => NProgress.done());
    }
    return promise;
}
