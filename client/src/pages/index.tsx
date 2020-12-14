import {faLock} from '@fortawesome/free-solid-svg-icons/faLock';
import {faMobileAlt} from '@fortawesome/free-solid-svg-icons/faMobileAlt';
import {faMoneyBillWave} from '@fortawesome/free-solid-svg-icons/faMoneyBillWave';
import {faRocket} from '@fortawesome/free-solid-svg-icons/faRocket';
import React from 'react';
import {pageWithAuth} from '../components/auth/with-auth';
import {Link, pageWithTranslation, Trans, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';
import {Icon} from '../components/ui/icon';
import {routes} from '../utils/routes';

require('../assets/css/pages/index.scss');
const heroBgImg = require('../assets/img/hero-bg.jpg');

function IndexPage({t}: WithTranslation) {
    return (
        <MainLayout
            id='index'
            title={t('common:tag') + ' | ' + t('common:name')}
            fullTitle={true}
            topNavigation={{transparent: true}}
        >
            <section className='home-hero'>
                <img className='hero-bg' src={heroBgImg}/>
                <ul className="bg-bubbles">
                    <li><Icon icon={faMoneyBillWave}/></li>
                    <li><Icon icon={faMoneyBillWave}/></li>
                    <li><Icon icon={faMoneyBillWave}/></li>
                    <li><Icon icon={faMoneyBillWave}/></li>
                    <li><Icon icon={faMoneyBillWave}/></li>
                    <li><Icon icon={faMoneyBillWave}/></li>
                    <li><Icon icon={faMoneyBillWave}/></li>
                    <li><Icon icon={faMoneyBillWave}/></li>
                    <li><Icon icon={faMoneyBillWave}/></li>
                    <li><Icon icon={faMoneyBillWave}/></li>
                </ul>
                <div className='overview'>
                    <div className='title'>
                        <Trans i18nKey='index:catchphrase'>
                            <strong/>
                        </Trans>
                    </div>
                    <div className='cta'>
                        <Link {...routes.register()}>
                            <a className='cta-btn'>{t('index:start_now')}</a>
                        </Link>
                    </div>
                </div>
            </section>
            <section className='home-section gray'>
                <div className='skew-bg top'/>
                <div className='container'>
                    <div className='home-content'>
                        <div className='icon'><Icon icon={faRocket}/></div>
                        <div className='content'>
                            <h2>{t('index:practical_and_fast')}</h2>
                            <p>{t('index:send_money_in_few_seconds')}</p>
                            <p>{t('index:no_delay')}</p>
                            <p>{t('index:registration_is_free')}</p>
                        </div>
                    </div>
                </div>
                <div className='skew-bg bottom'/>
            </section>
            <section className='home-section blue'>
                <div className='skew-bg top'/>
                <div className='container'>
                    <div className='home-content'>
                        <div className='content'>
                            <h2>{t('index:secure')}</h2>
                            <p>{t('index:best_cryptography')}</p>
                            <p>{t('index:easy_cancellation')}</p>
                        </div>
                        <div className='icon'><Icon icon={faLock}/></div>
                    </div>
                </div>
                <div className='skew-bg bottom'/>
            </section>
            <section className='home-section white'>
                <div className='container'>
                    <div className='home-content'>
                        <div className='icon'><Icon icon={faMobileAlt}/></div>
                        <div className='content'>
                            <h2>{t('index:usable_everywhere')}</h2>
                            <p>{t('index:pay_from_any_devices')}</p>
                            <a href='#'>{t('index:download_app')}</a>
                        </div>
                    </div>
                </div>
            </section>
        </MainLayout>
    );
}

export default pageWithAuth()(pageWithTranslation('index')(IndexPage));
