import Document, {DocumentContext, Head, Html, Main, NextScript} from 'next/document';
import React from 'react';

class MyDocument extends Document {
    static async getInitialProps(ctx: DocumentContext) {
        return Document.getInitialProps(ctx);
    }

    render() {
        const lang = this.props.__NEXT_DATA__.props.initialLanguage;
        return (
            <Html lang={lang}>
                <Head/>
                <body>
                    <Main/>
                    <NextScript/>
                </body>
            </Html>
        );
    }
}

export default MyDocument;
