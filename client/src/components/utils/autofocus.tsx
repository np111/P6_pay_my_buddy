import React from 'react';

export interface AutofocusProps {
    autoFocus?: boolean;
    children: React.ReactElement;
}

export class Autofocus extends React.PureComponent<AutofocusProps> {
    private _focused = false;

    componentDidUpdate(prevProps: Readonly<AutofocusProps>) {
        if (this.props.autoFocus !== prevProps.autoFocus) {
            this._focused = false;
        }
    }

    render() {
        const {children, autoFocus, ...inputProps} = this.props;
        if (autoFocus === false) {
            return React.cloneElement(children, {...inputProps, autoFocus: false});
        }

        const {ref: originalRef} = children as any;
        const ref = (input: HTMLElement) => {
            if (!this._focused && input && typeof input.focus === 'function') {
                this._focused = true;
                setTimeout(() => {
                    try {
                        input.focus();
                    } catch (ignored) {
                    }
                }, 0);
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
