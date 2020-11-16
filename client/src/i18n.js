const React = require('react');
const NextI18Next = require('next-i18next').default;
const hoistNonReactStatics = require('hoist-non-react-statics');

let buildId;
if (typeof window !== 'undefined') {
    /*
     * Locales are cached for a long time so we use the next build ID as query parameter to force-update this cache
     * with new builds.
     */
    buildId = window.__NEXT_DATA__.buildId;
    if (buildId === 'development') {
        buildId = Date.now().toString(16);
    }
}

const localeSubpaths = {
    en: 'en',
    fr: 'fr',
};
let otherLanguages = Object.keys(localeSubpaths);
const defaultLanguage = otherLanguages[0];
otherLanguages = otherLanguages.slice(1);
const i18NextInstance = new NextI18Next({
    localePath: typeof window === 'undefined' ? require('path').resolve('./public/static/locales') : 'static/locales',
    localeStructure: '{{lng}}/{{ns}}' + (buildId ? '.json?b=' + buildId : ''),
    defaultLanguage,
    otherLanguages,
    localeSubpaths,
    browserLanguageDetection: false,
});

// we don't use fallback
i18NextInstance.config.fallbackLng = false;
i18NextInstance.i18n.options.fallbackLng = false;

module.exports.i18nLocaleSubpaths = localeSubpaths;
module.exports.i18nNextInstance = i18NextInstance;
module.exports.appWithTranslation = i18NextInstance.appWithTranslation;
module.exports.withTranslation = i18NextInstance.withTranslation;
module.exports.pageWithTranslation = function pageWithTranslation(...args) {
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
};
module.exports.translationProps = function translationProps(props) {
    if (props.props) {
        props = props.props;
    }
    return {
        i18n: props.i18n,
        tReady: props.tReady,
        t: props.t,
    };
};
