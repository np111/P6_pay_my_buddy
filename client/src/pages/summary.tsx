import React, {useCallback} from 'react';
import useSWR from 'swr';
import {apiClient} from '../api/api-client';
import {UnhandledApiError} from '../api/api-exception';
import {CursorResponse, Transaction, UserBalance, UserBalancesResponse} from '../api/api-types';
import {pageWithAuth, withAuth, WithAuth} from '../components/auth/with-auth';
import {CurrencyAmount} from '../components/business/currency-amount';
import {Link, pageWithTranslation, withTranslation, WithTranslation} from '../components/i18n';
import {Heading} from '../components/layout/heading';
import {MainLayout} from '../components/layout/main-layout';
import {Card} from '../components/ui/card';
import {Col, Row} from '../components/ui/grid';
import {Icon} from '../components/ui/icon';
import {iconReceived} from '../components/ui/icons/icon-received';
import {iconSent} from '../components/ui/icons/icon-sent';
import {InlineError} from '../components/ui/inline-error';
import {Skeleton} from '../components/ui/skeleton';
import {DateFormat} from '../components/utils/date-format';
import {routes} from '../utils/routes';

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
                            <RecentActivity/>
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

    return (
        <Card title={t('summary:balance')}>
            {error ? (
                <InlineError error={error}/>
            ) : userBalance === undefined ? (
                <Skeleton/>
            ) : (
                <ul className='balances'>
                    {userBalance.balances.map(renderBalance)}
                </ul>
            )}
        </Card>
    );
}));

const RecentActivity = withAuth()(withTranslation('summary')(function RecentActivity({authGuard, t}: WithAuth & WithTranslation) {
    const {data: transactions, error} = useSWR(
        [authGuard.token, 'user/transaction?pageSize=5&pageSort=-id'],
        (authToken, url) => apiClient
            .fetch<CursorResponse<Transaction>>({authToken, url})
            .then((res) => {
                if (res.success === false) {
                    throw new UnhandledApiError(res.error);
                }
                return res.result;
            }),
        {refreshInterval: 60_000, errorRetryInterval: 15_000});

    const renderTransaction = useCallback((tr: Transaction) => {
        let title;
        let prefix;
        if (tr.recipient.id === authGuard.user.id) {
            title = <><Icon {...iconReceived}/> {tr.sender.name}</>;
            prefix = '';
        } else {
            title = <><Icon {...iconSent}/> {tr.recipient.name}</>;
            prefix = '-';
        }
        return (
            <li key={tr.id}>
                <div className='head'>
                    <div className='title'>{title}</div>
                    <div className='amount'>{prefix}<CurrencyAmount {...tr}/></div>
                </div>
                <div className='date'><DateFormat date={tr.date} format='lll'/></div>
            </li>
        );
    }, [authGuard]);

    return (
        <Card title={t('summary:recent_activity')}>
            {error ? (
                <InlineError error={error}/>
            ) : transactions === undefined ? (
                <Skeleton/>
            ) : (
                <>
                    <ul className='recent-activities'>
                        {transactions.records.map(renderTransaction)}
                    </ul>
                    <div className='text-center'>
                        <Link key='send-money' {...routes.activity()}>
                            <a>{t('summary:more_activities')}</a>
                        </Link>
                    </div>
                </>
            )}
        </Card>
    );
}));
