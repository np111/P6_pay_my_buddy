import React, {useCallback, useState} from 'react';
import useSWR from 'swr';
import {apiClient} from '../api/api-client';
import {UnhandledApiError} from '../api/api-exception';
import {pageWithAuth, WithAuth, withAuth} from '../components/auth/with-auth';
import {CurrencyAmount} from '../components/business/currency-amount';
import {pageWithTranslation, withTranslation, WithTranslation} from '../components/i18n';
import {Heading} from '../components/layout/heading';
import {MainLayout} from '../components/layout/main-layout';
import {Button} from '../components/ui/button';
import {Card} from '../components/ui/card';
import {DescList, DescListItem} from '../components/ui/desclist';
import {Icon} from '../components/ui/icon';
import {iconPageNext} from '../components/ui/icons/icon-page-next';
import {iconPagePrev} from '../components/ui/icons/icon-page-prev';
import {iconReceived} from '../components/ui/icons/icon-received';
import {iconSent} from '../components/ui/icons/icon-sent';
import {ColumnsType, ExpandableConfig, Table} from '../components/ui/table';
import {DateFormat} from '../components/utils/date-format';
import {titleSep} from '../utils/i18n-utils';
import {queryStringify} from '../utils/query-utils';
import {useStickyResult} from '../utils/react-utils';

function ActivityPage({t}: WithTranslation) {
    return (
        <MainLayout id='activity' title={t('common:page.activity')}>
            <div className='container'>
                <Heading
                    className='sm-t'
                    title={t('activity:transaction_history')}
                />
                <section className='sm-t'>
                    <TransactionsList/>
                </section>
            </div>
        </MainLayout>
    );
}

export default pageWithAuth({preAuthorize: 'isAuthenticated'})(pageWithTranslation('activity')(ActivityPage));

const TransactionsList = withAuth()(withTranslation('activity')(function TransactionsList({authGuard, t}: WithAuth & WithTranslation) {
    const authToken = authGuard.token;

    const [tableQuery, setTableQuery] = useState({
        pageSize: 10,
        pageSort: '-id',
        cursor: undefined,
    });

    const {data: loadingData, error, mutate} = useSWR(
        [authToken, 'user/transaction', tableQuery],
        (authToken, url, tableQuery) => apiClient
            .fetch({authToken, url: url + queryStringify(tableQuery)})
            .then((res) => {
                if (res.success === false) {
                    throw new UnhandledApiError(res.error);
                }
                return res.result;
            }),
        {refreshInterval: 60_000, errorRetryInterval: 15_000});
    const data = useStickyResult(loadingData);

    const handleTableChange = useCallback((pagination, filters, sorter) => {
        setTableQuery((tableQuery) => ({
            ...tableQuery,
            pageSort: !sorter.order ? undefined : (sorter.order === 'descend' ? '-' : '') + sorter.field,
        }));
    }, []);
    const gotoPrev = useCallback(() => {
        setTableQuery((tableQuery) => ({...tableQuery, cursor: data.prevCursor}));
    }, [data]);
    const gotoNext = useCallback(() => {
        setTableQuery((tableQuery) => ({...tableQuery, cursor: data.nextCursor}));
    }, [data]);

    if (error) {
        return <>TODO: inline error component</>;
    }

    const columns: ColumnsType<any> = [{
        key: 'type',
        title: t('activity:type'),
        render(none, r) {
            return r.recipient.id === authGuard.user.id
                ? <><Icon {...iconReceived}/> {t('activity:received_from', r.sender)}</>
                : <><Icon {...iconSent}/> {t('activity:sent_to', r.sender)}</>;
        },
    }, {
        dataIndex: 'description',
        title: t('activity:description'),
        render(description) {
            description = description.trim();
            const maxLen = 30;
            return description.length > maxLen ? description.substring(0, maxLen) + '…' : description;
        },
    }, {
        dataIndex: 'amount',
        title: t('activity:amount'),
        align: 'right',
        render(amount, r) {
            let prefix;
            if (r.recipient.id !== authGuard.user.id) {
                prefix = '- ';
            }
            return <>{prefix}<CurrencyAmount amount={amount} currency={r.currency}/></>;
        },
    }, {
        dataIndex: 'date',
        title: t('activity:date'),
        align: 'right',
        render(date) {
            return <DateFormat date={date} format='lll'/>;
        },
    }];
    const expandable: ExpandableConfig<any> = {
        expandRowByClick: true,
        expandedRowRender(r) {
            return (
                <DescList className='default horizontal secondary-title'>
                    <DescListItem
                        title={titleSep(t, 'activity:sender')}
                        value={r.sender.name + ' (' + r.sender.email + ')'}
                    />
                    <DescListItem
                        title={titleSep(t, 'activity:recipient')}
                        value={r.recipient.name + ' (' + r.recipient.email + ')'}
                    />
                    <DescListItem
                        title={titleSep(t, 'activity:amount')}
                        value={<CurrencyAmount amount={r.amount} currency={r.currency}/>}
                    />
                    <DescListItem
                        title={titleSep(t, 'activity:fee')}
                        value={<CurrencyAmount amount={r.fee} currency={r.currency}/>}
                    />
                    <DescListItem
                        title={titleSep(t, 'activity:date')}
                        value={<DateFormat date={r.date} format='llll'/>}
                    />
                    <DescListItem
                        title={titleSep(t, 'activity:description')}
                        value={r.description}
                    />
                </DescList>
            );
        },
    };
    return (
        <Card>
            <div className='card-narrow-table'>
                <Table
                    columns={columns}
                    expandable={expandable}
                    rowKey='id'
                    dataSource={!data ? undefined : data.records}
                    loading={loadingData === undefined}
                    pagination={false}
                    showSorterTooltip={false}
                    onChange={handleTableChange}
                />
                {!data ? undefined : (
                    <div className='cursor-pagination'>
                        <Button disabled={!data.hasPrev} onClick={gotoPrev}>
                            <Icon {...iconPagePrev}/>
                        </Button>
                        <Button disabled={!data.hasNext} onClick={gotoNext}>
                            <Icon {...iconPageNext}/>
                        </Button>
                    </div>
                )}
            </div>
        </Card>
    );
}));
