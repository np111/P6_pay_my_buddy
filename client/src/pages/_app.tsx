import {AppContext, AppProps} from 'next/app';
import {Router} from 'next/router';
import NProgress from 'nprogress';
import React, {Component} from 'react';
import {appWithAuth} from '../components/auth/app-with-auth';
import {appWithTranslation} from '../components/i18n';

if (typeof window !== 'undefined') {
    // Global: Progress
    Router.events.on('routeChangeStart', () => NProgress.start());
    Router.events.on('routeChangeComplete', () => NProgress.done());
    Router.events.on('routeChangeError', () => NProgress.done());
}

class MyApp extends React.Component<AppProps> {
    public static async getInitialProps({Component, ctx}: AppContext) {
        let pageProps: any = Component.getInitialProps ? await Component.getInitialProps(ctx) : {};
        if (!pageProps) {
            pageProps = {};
        }
        if (typeof window !== 'undefined') {
            // always remount component when getInitialProps is called (fix https://github.com/zeit/next.js/issues/2819)
            pageProps.key = ((window as any).__NEXT_PAGE_ID = ((window as any).__NEXT_PAGE_ID || 0) + 1);
        }
        return {pageProps};
    }

    public componentDidMount() {
        setTimeout(() => document.body.classList.add('is-loaded'), 0); // used to skip some transitions on page load
    }

    public render() {
        return (
            <>
                {this.renderPage()}

                {/* Fix Chromium FOUC https://github.com/zeit/next-plugins/issues/455 */}
                <script>{' '}</script>
            </>
        );
    }

    private renderPage = () => {
        const {Component, pageProps} = this.props;
        return <Component {...pageProps} />;
    };
}

export default appWithAuth()(appWithTranslation(MyApp));
