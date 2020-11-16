/*
 * Note: This error page must be self-contained with no dependencies.
 */

import {NextPageContext} from 'next';
import Head from 'next/head';
import React from 'react';

const bgColor = '#fff';
const textColor = '#111';
const btnBgColor = '#1890ff';
const btnTextColor = '#fff';
const style = `
@import url('https://fonts.googleapis.com/css?family=Montserrat:200,400&display=swap');

html, body {
  background: ${bgColor};
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

#error {
  display: flex;
  min-width: 100vw;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
}

#error .error-box {
  position: relative;
  font-family: 'Montserrat', sans-serif;
  text-align: center;
}

#error .error-title {
  position: absolute;
  left: 50%;
  top: 0;
  transform: translateX(-50%);
  font-size: 180px;
  font-size: min(30vw, 180px);
  line-height: 180px;
  font-weight: 200;
  color: ${textColor};
  text-transform: uppercase;
  white-space: nowrap;
}

#error .error-message {
  font-size: 21px;
  text-transform: uppercase;
  color: ${textColor};
  background: ${bgColor};
  padding: 5px 10px;
  margin-top: 128px;
  position: relative;
  z-index: 1;
}

#error .error-btn {
  display: inline-block;
  margin-top: 15px;
  text-decoration: none;
  color: ${btnTextColor};
  text-transform: uppercase;
  padding: 10px 20px;
  background: ${btnBgColor};
  font-size: 18px;
  position: relative;
  z-index: 1;
}
`.replace(/^\s+/gm, '').replace(/\s+$/gm, '').replace(/\r?\n/g, '');

const messages = {
    en: {
        error: 'Error',
        home: 'Go to Home',
        back: 'Go Back',
        status: {
            404: ['Not Found', 'The page does not exist'],
            500: ['Internal Error', 'An internal error occurred'],
        },
    },
    fr: {
        error: 'Erreur',
        home: 'Aller à l\'accueil',
        back: 'Retour',
        status: {
            404: ['Introuvable', 'La page n\'existe pas'],
            500: ['Erreur interne', 'Une erreur interne s\'est produite'],
        },
    },
};

export interface MyErrorProps {
    statusCode?: number;
    reqUrl?: string;
    backUrl?: string;
}

export default class MyError extends React.Component<MyErrorProps> {
    static getInitialProps({asPath, req, res, err}: NextPageContext) {
        let statusCode: number | undefined;
        if (res) {
            statusCode = res.statusCode || 500;
        } else if (err) {
            statusCode = err.statusCode;
        }

        let backUrl: string | undefined;
        if (!backUrl && asPath !== '' && asPath !== '/' && asPath !== '/index') {
            backUrl = '/';
        }
        return {statusCode, reqUrl: req ? req.url : undefined, backUrl, namespacesRequired: []};
    }

    private getLocalizedMessages(reqUrl?: string) {
        if (!reqUrl && typeof window !== 'undefined') {
            reqUrl = window.location.pathname;
        }
        if (reqUrl) {
            const lang = reqUrl.replace(/^\/?([^\/]+)(?:\/.*|$)/, '$1');
            if (Object.prototype.hasOwnProperty.call(messages, lang)) {
                return (messages as any)[lang] as any;
            }
        }
        return messages.en;
    }

    public render() {
        const {statusCode, reqUrl, backUrl} = this.props;
        const m = this.getLocalizedMessages(reqUrl);
        const status = m.status['' + statusCode] || m.status['500'];
        return (
            <>
                <Head>
                    <title>{statusCode ? m.error + ' ' + statusCode + ' (' + status[0] + ')' : status[0]}</title>
                    <style type='text/css'>{style}</style>
                </Head>
                <div id='error'>
                    <div className='error-box'>
                        <div className='error-title'>OOPS!</div>
                        <div className='error-message'>{(statusCode ? statusCode + ' - ' : '') + status[1]}</div>
                        {!backUrl ? undefined : (
                            <a href={backUrl} className='error-btn'>{backUrl === '/' ? m.home : m.back}</a>
                        )}
                    </div>
                </div>
            </>
        );
    }
}
