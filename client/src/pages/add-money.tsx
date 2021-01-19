import React from 'react';
import {pageWithAuth} from '../components/auth/with-auth';
import {pageWithTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';
import {Card} from '../components/ui/card';
import '../assets/css/pages/send-money.scss';

function AddMoneyPage({t}: WithTranslation) {
    const title = t('send-money:add_money');
    return (
        <MainLayout id='add-money' title={title}>
            <div className='container'>
                <section className='sm-t'>
                    <Card className='add-money-card'>
                        <h1>{title}</h1>
                        <p>{t('send-money:add_money_transfer_desc')}</p>
                        <table className='add-money-table'>
                            <tr>
                                <th>{t('send-money:reference_number')}</th>
                                <td>XXXXXXXXXXXX</td>
                            </tr>
                            <tr>
                                <th>{t('send-money:recipient_iban')}</th>
                                <td>XXXXXXXXXXXXXXXXXXXX</td>
                            </tr>
                            <tr>
                                <th>{t('send-money:recipient_name')}</th>
                                <td>XXXXXXXX</td>
                            </tr>
                            <tr>
                                <th>{t('send-money:swift_bic')}</th>
                                <td>XXXXXXXX</td>
                            </tr>
                        </table>
                    </Card>
                </section>
            </div>
        </MainLayout>
    );
}

export default pageWithAuth({preAuthorize: 'isAuthenticated'})(pageWithTranslation('send-money')(AddMoneyPage));
