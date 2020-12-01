import {Col, Row} from 'antd/lib/grid';
import React from 'react';
import {Trans, withTranslation, WithTranslation} from '../i18n';

export const Footer = withTranslation('common')(function ({t}: WithTranslation) {
    const copyYear = Math.max(2020, new Date().getFullYear());
    const colProps = {xs: 24, sm: 12, lg: 6};
    const renderLinks = () => {
        // TODO
        return null;
    };
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
                            <a href='/contact'>
                                {t('common:footer.contact')}
                            </a>
                        </Col>
                        <Col {...colProps}>
                            <div className='title'>{t('common:footer.links')}</div>
                            {renderLinks()}
                        </Col>
                        <Col {...colProps}>
                            {/* empty */}
                        </Col>
                        <Col {...colProps}>
                            <div className='title'>{t('common:footer.help')}</div>
                            {renderLinks()}
                        </Col>
                    </Row>
                </div>
            </div>
            <div className='copyright'>
                <div className='container'>
                    <Row>
                        <Col xs={24} sm={12}>
                            <Trans i18nKey='common:footer.copyright'>
                                {copyYear}
                                <a href='/'/>
                            </Trans>
                        </Col>
                        <Col className='right' xs={24} sm={12}>
                            <a href='/tos' target='_blank'>
                                {t('common:footer.tos')}
                            </a>
                        </Col>
                    </Row>
                </div>
            </div>
        </footer>
    );
});
