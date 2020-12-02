import React from 'react';

export interface AutofocusProps {
    children: React.ReactElement;
}

export class Autofocus extends React.PureComponent<AutofocusProps> {
    private _focused = false;

    render() {
        const {children, ...inputProps} = this.props;
        const {ref: originalRef} = children as any;
        const ref = (input: HTMLElement) => {
            if (!this._focused && input && typeof input.focus === 'function') {
                this._focused = true;
                input.focus();
            }
            if (originalRef) {
                if (typeof originalRef === 'function') {
                    originalRef(input);
                } else {
                    originalRef.current = input;
                }
            }
        };
        return React.cloneElement(children, {...inputProps, ref, autoFocus: true});
    }
}
