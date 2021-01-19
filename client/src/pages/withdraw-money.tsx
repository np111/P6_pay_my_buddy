import React, {useCallback, useEffect, useState} from 'react';
import {apiClient} from '../api/api-client';
import {currencies, currenciesOptions} from '../api/api-constants';
import {UnhandledApiError} from '../api/api-exception';
import {Transaction} from '../api/api-types';
import '../assets/css/pages/send-money.scss';
import {pageWithAuth, WithAuth, withAuth} from '../components/auth/with-auth';
import {Link, pageWithTranslation, Trans, withTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';
import {Button} from '../components/ui/button';
import {Card} from '../components/ui/card';
import {Form, useForm} from '../components/ui/form';
import {Icon} from '../components/ui/icon';
import {iconSendMoney} from '../components/ui/icons/icon-sendmoney';
import {Input} from '../components/ui/input';
import {Select} from '../components/ui/select';
import {Spin} from '../components/ui/spin';
import {Autofocus} from '../components/utils/autofocus';
import {setFormError} from '../utils/form-utils';
import {useCatchAsyncError} from '../utils/react-utils';
import {routes} from '../utils/routes';

function WithdrawMoneyPage({t}: WithTranslation) {
    const [withdrawn, setWithdrawn] = useState<boolean>(false);
    const handleWithdrawn = useCallback(() => setWithdrawn(true), []);

    const title = withdrawn ? t('send-money:money_withdrawn') : t('send-money:withdraw_money');
    return (
        <MainLayout id='withdraw-money' title={title}>
            <div className='container'>
                <section className='sm-t'>
                    <Card className='send-money-card'>
                        <h1>{title}</h1>
                        {withdrawn ? (
                            <WithdrawnResume/>
                        ) : (
                            <WithdrawMoneyForm onComplete={handleWithdrawn}/>
                        )}
                    </Card>
                </section>
            </div>
        </MainLayout>
    );
}

export default pageWithAuth({preAuthorize: 'isAuthenticated'})(pageWithTranslation('send-money')(WithdrawMoneyPage));

export interface WithdrawMoneyFormProps {
    onComplete: () => void;
}

const WithdrawMoneyForm = withAuth()(withTranslation('send-money')(function WithdrawMoneyForm({onComplete, authGuard, t}: WithdrawMoneyFormProps & WithAuth & WithTranslation) {
    const decimalSep = t('common:intl.decimalSep', '.');
    const authToken = authGuard.token;
    const catchAsyncError = useCatchAsyncError();

    const [loading, setLoading] = useState(false);
    const [form] = useForm();
    const addContact = useCallback(({currency, amount, iban}) => {
        setLoading(true);
        return apiClient.fetch<Transaction>({
            authToken,
            url: 'user/withdraw-to-bank',
            body: {
                currency,
                amount: amount.replace(decimalSep, '.'),
                iban,
            },
        }).then((res): void | Promise<any> => {
            if (res.success === false) {
                if (res.error.code === 'NOT_ENOUGH_FUNDS') {
                    return setFormError(form, 'amount', t('send-money:not_enough_funds', res.error.metadata));
                }
                throw new UnhandledApiError(res.error);
            }

            return onComplete();
        }).catch(catchAsyncError).finally(() => setLoading(false));
    }, [authToken, onComplete, t, catchAsyncError, form, decimalSep]);

    const [currency, setCurrency] = useState(authGuard.user.defaultCurrency);
    const handleChangeCurrency = useCallback((value: string) => {
        setCurrency(value);
    }, []);

    const normalizeAmount = useCallback((value: string) => {
        const decimals = currencies[currency].decimals;
        value = value.replace(/[^0-9]/g, '').replace(/^0+/, '');
        if (value.length < decimals + 1) {
            value = '0'.repeat(decimals + 1 - value.length) + value;
        }
        return decimals === 0 ? value
            : value.substring(0, value.length - decimals) + decimalSep + value.substring(value.length - decimals);
    }, [currency, decimalSep]);
    useEffect(() => {
        form.setFieldsValue({amount: normalizeAmount(form.getFieldValue('amount'))});
    }, [form, normalizeAmount, currency]);

    return (
        <Spin spinning={loading}>
            <Form
                form={form}
                onFinish={addContact}
                layout='horizontal'
            >
                <Form.Item
                    className='amount-item'
                    name='amount'
                    validateTrigger='onSubmit'
                    initialValue={normalizeAmount('')}
                    normalize={normalizeAmount}
                    rules={[{required: true, pattern: /[1-9]/, message: t('send-money:amount_must_be_positive')}]}
                >
                    <Autofocus>
                        <Input placeholder={t('send-money:amount')} size='large'/>
                    </Autofocus>
                </Form.Item>
                <Form.Item
                    className='currency-item'
                    name='currency'
                    initialValue={currency}
                >
                    <Select options={currenciesOptions} onChange={handleChangeCurrency}/>
                </Form.Item>
                <Form.Item
                    name='iban'
                    validateTrigger='onBlur'
                    rules={[{required: true, message: t('send-money:iban_required')}]}
                >
                    <Input placeholder={t('send-money:iban')} maxLength={200}/>
                </Form.Item>
                <Form.Item>
                    <Button type='primary' htmlType='submit' size='large' block={true}>
                        {t('send-money:withdraw')}
                    </Button>
                </Form.Item>
            </Form>
        </Spin>
    );
}));

function WithdrawnResume() {
    return (
        <>
            <p>
                <Trans i18nKey='send-money:withdraw_details'/>
            </p>
            <Link key='send-money' {...routes.withdrawMoney()}>
                <Button type='primary'>
                    <Icon {...iconSendMoney} marginRight={true}/><Trans i18nKey='send-money:withdraw_again'/>
                </Button>
            </Link>
        </>
    );
}
