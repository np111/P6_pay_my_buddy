import React, {useMemo} from 'react';

export interface AutofocusProps {
    autoFocus?: boolean;
    children?: React.ReactNode;
}

export function Autofocus({children, autoFocus, ...inputProps}: AutofocusProps) {
    autoFocus = autoFocus !== false;
    const state = useMemo(() => ({applied: false}), [children, autoFocus]);
    const renderChildren = (child: React.ReactElement) => {
        (inputProps as any).ref = (input: HTMLElement) => {
            if (!state.applied && autoFocus && input && typeof input.focus === 'function') {
                state.applied = true;
                input.focus();
            }
            // @ts-ignore
            const {ref} = child;
            if (ref) {
                if (typeof ref === 'function') {
                    ref(input);
                } else {
                    ref.current = input;
                }
            }
        };
        return React.cloneElement(child, {...inputProps, autoFocus});
    };
    return React.Children.map(children as any, renderChildren);
}
