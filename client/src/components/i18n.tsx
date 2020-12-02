import hoistNonReactStatics from 'hoist-non-react-statics';
import NextI18Next, {WithTranslation as NextI18WithTranslation} from 'next-i18next';
import {LinkProps as NextLinkProps} from 'next/dist/client/link';
import React, {memo} from 'react';
import {i18nNextInstance} from '../i18n-instance';

const i18NextInstance: NextI18Next = require('../i18n-instance').i18nNextInstance;

export const appWithTranslation = i18NextInstance.appWithTranslation;

export type WithTranslation = NextI18WithTranslation;
export const withTranslation = i18NextInstance.withTranslation;

export function pageWithTranslation(...args) {
    const namespaces = ['common', ...args];
    const withTranslation = i18NextInstance.withTranslation.call(this, namespaces);
    return (WrappedComponent) => {
        WrappedComponent = withTranslation(WrappedComponent);

        class PageWithTranslation extends React.Component {
            static async getInitialProps(...initialPropsArgs) {
                return {
                    ...(WrappedComponent.getInitialProps
                        ? await WrappedComponent.getInitialProps.apply(this, initialPropsArgs) : undefined),
                    namespacesRequired: namespaces,
                };
            }

            render() {
                return React.createElement(WrappedComponent, this.props);
            }
        }

        return hoistNonReactStatics(PageWithTranslation, WrappedComponent, {getInitialProps: true});
    };
}

export function translationProps(props) {
    if (props.props) {
        props = props.props;
    }
    return {
        i18n: props.i18n,
        tReady: props.tReady,
        t: props.t,
    };
}

export const Trans = i18NextInstance.Trans;
export const Router = i18NextInstance.Router;

export type LinkProps = NextLinkProps;
export const Link = memo(function Link(props: LinkProps & { children: React.ReactNode }) {
    const {children} = props;
    let {passHref} = props;
    if (passHref === undefined && isButton(children)) {
        passHref = true;
    }
    return <i18nNextInstance.Link {...props} passHref={passHref}/>;
});

function isButton(children: any) {
    return !!(children && children.type && children.type.__ANT_BUTTON);
}
