import React from 'react';
import {Trans, withTranslation, WithTranslation} from '../i18n';
import {Col, Row} from '../ui/grid';

require('../../assets/css/layouts/footer.scss');

export const Footer = withTranslation('common')(function ({t}: WithTranslation) {
    const copyYear = Math.max(2020, new Date().getFullYear());
    const colProps = {xs: 24, sm: 12, lg: 6};
    return (
        <footer id='footer'>
            <div className='links'>
                <div className='container'>
                    <Row>
                        <Col {...colProps}>
                            <div className='title'>{t('common:name')}</div>
                            <p>
                                <Trans i18nKey='common:footer.about'>
                                    <a href='/'/>
                                </Trans>
                            </p>
                        </Col>
                        <Col {...colProps}>
                            {/* empty */}
                        </Col>
                        <Col {...colProps}>
                            <div className='title'>{t('common:footer.links')}</div>
                            <ul>
                                <li><a href='#'>{t('common:footer.prices')}</a></li>
                                <li><a href='#'>{t('common:footer.developers')}</a></li>
                                <li><a href='#'>{t('common:footer.partners')}</a></li>
                            </ul>
                        </Col>
                        <Col {...colProps}>
                            <div className='title'>{t('common:footer.help')}</div>
                            <ul>
                                <li><a href='#'>{t('common:footer.community')}</a></li>
                                <li><a href='#'>{t('common:footer.support')}</a></li>
                            </ul>
                        </Col>
                    </Row>
                </div>
            </div>
            <div className='copyright'>
                <div className='container'>
                    <Row>
                        <Col xs={24} sm={12}>
                            <Trans i18nKey='common:footer.copyright'>
                                {'' + copyYear}
                                <a href='#'/>
                            </Trans>
                        </Col>
                        <Col className='right' xs={24} sm={12}>
                            <a href='#' target='_blank'>{t('common:footer.tos')}</a>
                        </Col>
                    </Row>
                </div>
            </div>
        </footer>
    );
});
