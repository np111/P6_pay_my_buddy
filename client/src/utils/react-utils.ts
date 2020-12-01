import React, {useState} from 'react';

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
