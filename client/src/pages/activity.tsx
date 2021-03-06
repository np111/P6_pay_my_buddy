import React, {useCallback, useState} from 'react';
import useSWR from 'swr';
import {apiClient} from '../api/api-client';
import {UnhandledApiError} from '../api/api-exception';
import {CursorResponse, Transaction} from '../api/api-types';
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
import {InlineError} from '../components/ui/inline-error';
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
            .fetch<CursorResponse<Transaction>>({authToken, url: url + queryStringify(tableQuery)})
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
        return <InlineError error={error}/>;
    }

    const columns: ColumnsType<Transaction> = [{
        key: 'type',
        title: t('activity:type'),
        render(none, tr) {
            return tr.recipient.id === authGuard.user.id
                ? <><Icon {...iconReceived}/> {t('activity:received_from', tr.sender)}</>
                : <><Icon {...iconSent}/> {t('activity:sent_to', tr.recipient)}</>;
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
        render(amount, tr) {
            let prefix;
            if (tr.recipient.id !== authGuard.user.id) {
                prefix = '- ';
            }
            return <>{prefix}<CurrencyAmount amount={amount} currency={tr.currency}/></>;
        },
    }, {
        dataIndex: 'date',
        title: t('activity:date'),
        align: 'right',
        render(date) {
            return <DateFormat date={date} format='lll'/>;
        },
    }];
    const expandable: ExpandableConfig<Transaction> = {
        expandRowByClick: true,
        expandedRowRender(tr) {
            return (
                <DescList className='default horizontal secondary-title'>
                    <DescListItem
                        title={titleSep(t, 'activity:sender')}
                        value={tr.sender.name + ' (' + tr.sender.email + ')'}
                    />
                    <DescListItem
                        title={titleSep(t, 'activity:recipient')}
                        value={tr.recipient.name + ' (' + tr.recipient.email + ')'}
                    />
                    <DescListItem
                        title={titleSep(t, 'activity:amount')}
                        value={<CurrencyAmount amount={tr.amount} currency={tr.currency}/>}
                    />
                    <DescListItem
                        title={titleSep(t, 'activity:fee')}
                        value={<CurrencyAmount amount={tr.fee} currency={tr.currency}/>}
                    />
                    <DescListItem
                        title={titleSep(t, 'activity:date')}
                        value={<DateFormat date={tr.date} format='llll'/>}
                    />
                    <DescListItem
                        title={titleSep(t, 'activity:description')}
                        value={tr.description}
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
