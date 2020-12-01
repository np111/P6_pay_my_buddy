import hoistNonReactStatics from 'hoist-non-react-statics';
import NextI18Next, {WithTranslation as NextI18WithTranslation} from 'next-i18next';
import React from 'react';

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
