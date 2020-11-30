const {ContextReplacementPlugin} = require('webpack');
const {nextI18NextRewrites} = require('next-i18next/rewrites');
const {i18nLocaleSubpaths} = require('./src/i18n');
const withCss = require('@zeit/next-css');
const withSass = require('@zeit/next-sass');
const withLess = require('@zeit/next-less');
const withBundleAnalyzer = require('@next/bundle-analyzer')({
    enabled: process.env.ANALYZE === 'true',
});

module.exports = withBundleAnalyzer(withSass(withLess(withCss({
    distDir: process.env.NODE_ENV === 'production' ? '.next-production' : '.next-development',
    poweredByHeader: false,
    lessLoaderOptions: {
        javascriptEnabled: true,
        modifyVars: require('./src/assets/css/_antd.theme'),
    },
    webpack(conf, {defaultLoaders, isServer, dev}) {
        // Handle assets files (images/fonts)
        conf.module.rules.push({
            test: /\.(ico|gif|png|jpg|jpeg|svg|webp|webm|mp4|woff|woff2|ttf|eot)$/,
            use: [
                {
                    loader: 'file-loader',
                    options: {
                        publicPath: (url) => '/_next/static/' + url,
                        outputPath: 'static/',
                        name: !dev ? '[hash:10].[ext]' : '[path][name].[ext]',
                        emitFile: true,
                        esModule: false,
                    },
                },
            ],
        });

        // Minimize CSS files
        if (conf.optimization.minimizer) {
            const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin');
            conf.optimization.minimizer.push(new OptimizeCssAssetsPlugin({
                cssProcessor: require('cssnano'),
                cssProcessorPluginOptions: {
                    preset: ['default', {discardComments: {removeAll: true}}],
                },
                canPrint: true,
            }));
        }

        // Only include used moment data
        conf.plugins.push(new ContextReplacementPlugin(/moment[/\\]locale$/, /en|fr/));

        return conf;
    },
    rewrites: async () => nextI18NextRewrites(i18nLocaleSubpaths),
    serverRuntimeConfig: {
        apiUrl: process.env.SERVER_API_URL || undefined,
    },
    publicRuntimeConfig: {
        apiUrl: process.env.CLIENT_API_URL || 'http://127.0.0.1:8081/',
    },
}))));
