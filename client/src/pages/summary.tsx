import React, {useCallback} from 'react';
import useSWR from 'swr';
import {apiClient} from '../api/api-client';
import {UnhandledApiError} from '../api/api-exception';
import {UserBalance, UserBalancesResponse} from '../api/api-types';
import {pageWithAuth, withAuth, WithAuth} from '../components/auth/with-auth';
import {CurrencyAmount} from '../components/business/currency-amount';
import {pageWithTranslation, withTranslation, WithTranslation} from '../components/i18n';
import {Heading} from '../components/layout/heading';
import {MainLayout} from '../components/layout/main-layout';
import {Card} from '../components/ui/card';
import {Col, Row} from '../components/ui/grid';
import {Skeleton} from '../components/ui/skeleton';

require('../assets/css/pages/summary.scss');

function SummaryPage({t, authGuard}: WithAuth & WithTranslation) {
    return (
        <MainLayout id='summary' title={t('common:page.summary')}>
            <div className='container'>
                <Heading
                    className='sm-t'
                    title={t('summary:welcome', {name: getFirstName(authGuard.user.name)})}
                />
                <Row>
                    <Col xs={24} md={14}>
                        <section className='sm-t'>
                            <Balances/>
                        </section>
                    </Col>
                    <Col xs={24} md={10}>
                        <section className='sm-t'>
                            <Card title='Transactions récentes'>TODO</Card>
                        </section>
                    </Col>
                </Row>
            </div>
        </MainLayout>
    );
}

export default pageWithAuth({preAuthorize: 'isAuthenticated'})(pageWithTranslation('summary')(SummaryPage));

function getFirstName(name: string) {
    const sepPos = name.indexOf(' ');
    return sepPos === -1 ? name : name.substring(0, sepPos);
}

const Balances = withAuth()(withTranslation('summary')(function Balances({authGuard, t}: WithAuth & WithTranslation) {
    const {data: userBalance, error} = useSWR(
        [authGuard.token, 'user/balance'],
        (authToken, url) => apiClient
            .fetch<UserBalancesResponse>({authToken, url})
            .then((res) => {
                if (res.success === false) {
                    throw new UnhandledApiError(res.error);
                }
                return res.result;
            }),
        {refreshInterval: 60_000, errorRetryInterval: 15_000});

    const renderBalance = useCallback((balance: UserBalance) => {
        return <li key={balance.currency}><CurrencyAmount {...balance}/></li>;
    }, []);

    if (error) {
        return <>TODO: inline error component</>;
    }
    if (userBalance === undefined) {
        return <Skeleton/>;
    }
    return (
        <Card title={t('summary:balance')}>
            <ul className='balances'>
                {userBalance.balances.map(renderBalance)}
            </ul>
        </Card>
    );
}));
