import React, {useCallback, useEffect, useState} from 'react';
import {apiClient} from '../api/api-client';
import {currencies, currenciesOptions} from '../api/api-constants';
import {UnhandledApiError} from '../api/api-exception';
import {Contact, ListResponse, Transaction} from '../api/api-types';
import '../assets/css/pages/send-money.scss';
import {pageWithAuth, WithAuth, withAuth} from '../components/auth/with-auth';
import {CurrencyAmount} from '../components/business/currency-amount';
import {Link, pageWithTranslation, Trans, withTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';
import {AutoComplete} from '../components/ui/auto-complete';
import {Button} from '../components/ui/button';
import {Card} from '../components/ui/card';
import {Form, useForm} from '../components/ui/form';
import {Icon} from '../components/ui/icon';
import {iconSendMoney} from '../components/ui/icons/icon-sendmoney';
import {Input, TextArea} from '../components/ui/input';
import {Select} from '../components/ui/select';
import {Spin} from '../components/ui/spin';
import {Autofocus} from '../components/utils/autofocus';
import {setFormError} from '../utils/form-utils';
import {noop, useCatchAsyncError} from '../utils/react-utils';
import {routes} from '../utils/routes';

function SendMoneyPage({t}: WithTranslation) {
    const [contact, setContact] = useState<Contact>();
    const handleContact = useCallback((contact: Contact) => setContact(contact), []);
    const clearContact = useCallback(() => setContact(undefined), []);

    const [transaction, setTransaction] = useState<Transaction>();
    const handleSent = useCallback((transaction) => setTransaction(transaction), []);

    const title = transaction ? t('send-money:money_sent') : t('send-money:send_money');
    return (
        <MainLayout id='send-money' title={title}>
            <div className='container'>
                <section className='sm-t'>
                    <Card className='send-money-card'>
                        <h1>{title}</h1>
                        {transaction ? (
                            <SendResume transaction={transaction}/>
                        ) : contact ? (
                            <SendMoneyForm contact={contact} onCancel={clearContact} onComplete={handleSent}/>
                        ) : (
                            <SelectContactForm onComplete={handleContact}/>
                        )}
                    </Card>
                </section>
            </div>
        </MainLayout>
    );
}

export default pageWithAuth({preAuthorize: 'isAuthenticated'})(pageWithTranslation('send-money')(SendMoneyPage));

export interface SelectContactFormProps {
    onComplete: (contact: Contact) => void;
}

const SelectContactForm = withAuth()(withTranslation('send-money')(function SelectContactForm({onComplete, authGuard, t}: SelectContactFormProps & WithAuth & WithTranslation) {
    const authToken = authGuard.token;
    const catchAsyncError = useCatchAsyncError();

    const [loading, setLoading] = useState(false);
    const [form] = useForm();
    const addContact = useCallback(({contact}) => {
        if (contact === undefined || contact === '') {
            return;
        }

        setLoading(true);
        return apiClient.fetch<Contact>({
            authToken,
            url: 'user/contact/' + encodeURIComponent(contact),
        }).then((res): void | Promise<any> => {
            if (res.success === false) {
                if (res.error.code === 'CONTACT_NOT_FOUND') {
                    return setFormError(form, 'contact', t('send-money:contact_not_found'));
                }
                throw new UnhandledApiError(res.error);
            }

            return onComplete(res.result);
        }).catch(catchAsyncError).finally(() => setLoading(false));
    }, [authToken, onComplete, t, catchAsyncError, form]);

    const [options, setOptions] = useState({counter: 0, values: []});
    const handleSelect = useCallback((value, {contact}) => {
        return onComplete(contact);
    }, [onComplete]);
    const handleSearch = useCallback((input: string) => {
        if (input === '') {
            return setOptions({...options, values: []});
        }

        const counter = options.counter;
        return apiClient.fetch<ListResponse<Contact>>({
            authToken,
            url: 'user/contact-autocomplete?input=' + encodeURIComponent(input),
        }).then((res): void | Promise<any> => {
            if (res.success === false) {
                return;
            }

            const values = res.result.records.map((contact) => ({
                contact,
                value: contact.email,
                label: contact.name + ' • ' + contact.email,
            }));
            setOptions((options) => {
                if (counter !== options.counter) {
                    return options;
                }
                return {...options, counter: counter + 1, values};
            });
        }).catch(noop);
    }, [authToken, options]);

    return (
        <Spin spinning={loading}>
            <Form
                form={form}
                onFinish={addContact}
                layout='horizontal'
            >
                <Form.Item
                    name='contact'
                    validateTrigger='onBlur'
                >
                    <AutoComplete options={options.values} onSearch={handleSearch} onSelect={handleSelect}>
                        <Input placeholder={t('send-money:contact')} maxLength={255}/>
                    </AutoComplete>
                </Form.Item>
                <Form.Item>
                    <Button type='primary' htmlType='submit' block={true}>
                        {t('send-money:next')}
                    </Button>
                </Form.Item>
            </Form>
        </Spin>
    );
}));

export interface SendMoneyFormProps {
    contact: Contact;
    onCancel: () => void;
    onComplete: (transaction: Transaction) => void;
}

const SendMoneyForm = withAuth()(withTranslation('send-money')(function SendMoneyForm({contact, onCancel, onComplete, authGuard, t}: SendMoneyFormProps & WithAuth & WithTranslation) {
    const decimalSep = t('common:intl.decimalSep', '.');
    const authToken = authGuard.token;
    const catchAsyncError = useCatchAsyncError();

    const [loading, setLoading] = useState(false);
    const [form] = useForm();
    const addContact = useCallback(({currency, amount, description}) => {
        setLoading(true);
        return apiClient.fetch<Transaction>({
            authToken,
            url: 'user/transaction',
            body: {
                recipientId: contact.id,
                currency,
                amount: amount.replace(decimalSep, '.'),
                description,
            },
        }).then((res): void | Promise<any> => {
            if (res.success === false) {
                if (res.error.code === 'NOT_ENOUGH_FUNDS') {
                    return setFormError(form, 'amount', t('send-money:not_enough_funds', res.error.metadata));
                }
                throw new UnhandledApiError(res.error);
            }

            return onComplete(res.result);
        }).catch(catchAsyncError).finally(() => setLoading(false));
    }, [authToken, onComplete, t, catchAsyncError, form, contact, decimalSep]);

    const [currency, setCurrency] = useState(authGuard.user.defaultCurrency);
    const handleChangeCurrency = useCallback((value: string) => {
        setCurrency(value);
    }, []);

    const normalizeAmount = useCallback((value: string) => {
        const decimals = currencies[currency].decimals;
        value = value.replaceAll(/[^0-9]/g, '').replace(/^0+/, '');
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
                <Form.Item>
                    {t('send-money:to', contact)}
                </Form.Item>
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
                    name='description'
                    validateTrigger='onBlur'
                    rules={[{required: true, message: t('send-money:description_required')}]}
                >
                    <TextArea placeholder={t('send-money:description')} maxLength={200}/>
                </Form.Item>
                <Form.Item>
                    <Button type='primary' htmlType='submit' size='large' block={true}>
                        {t('send-money:send')}
                    </Button>
                </Form.Item>
                <Form.Item>
                    <Button type='default' onClick={onCancel}>
                        {t('send-money:cancel')}
                    </Button>
                </Form.Item>
            </Form>
        </Spin>
    );
}));

interface SendResumeProps {
    transaction: Transaction;
}

function SendResume({transaction}: SendResumeProps) {
    return (
        <>
            <p>
                <Trans i18nKey='send-money:transaction_details'>
                    <CurrencyAmount currency={transaction.currency} amount={transaction.amount}/>
                    {transaction.recipient.name}
                </Trans>
            </p>
            <Link key='send-money' {...routes.sendMoney()}>
                <Button type='primary'>
                    <Icon {...iconSendMoney} marginRight={true}/><Trans i18nKey='send-money:send_again'/>
                </Button>
            </Link>
        </>
    );
}
