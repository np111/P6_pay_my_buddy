const NextI18Next = require('next-i18next').default;

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
