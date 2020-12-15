import {ColumnsType} from 'antd/lib/table/interface';
import React, {useCallback, useRef, useState} from 'react';
import useSWR from 'swr';
import {apiClient} from '../api/api-client';
import {UnhandledApiError} from '../api/api-exception';
import {Contact, PageResponse} from '../api/api-types';
import {pageWithAuth, WithAuth, withAuth} from '../components/auth/with-auth';
import {pageWithTranslation, withTranslation, WithTranslation} from '../components/i18n';
import {Heading} from '../components/layout/heading';
import {MainLayout} from '../components/layout/main-layout';
import {Button} from '../components/ui/button';
import {Card} from '../components/ui/card';
import {Form, useForm} from '../components/ui/form';
import {Icon} from '../components/ui/icon';
import {iconAddContact} from '../components/ui/icons/icon-addcontact';
import {InlineError} from '../components/ui/inline-error';
import {Input} from '../components/ui/input';
import {Modal} from '../components/ui/modal';
import {Popconfirm} from '../components/ui/popconfirm';
import {Spin} from '../components/ui/spin';
import {Table} from '../components/ui/table';
import {Autofocus} from '../components/utils/autofocus';
import {setFormError} from '../utils/form-utils';
import notification from '../utils/notification';
import {queryStringify} from '../utils/query-utils';
import {doBindArgs, noop, useCatchAsyncError, useStickyResult, withNProgress} from '../utils/react-utils';
import {AppRouter} from '../utils/routes';

const showAddParam = 'showAdd';

interface ContactsPageProps {
    showAdd?: boolean;
}

function ContactsPage({showAdd: initialShowAdd, t}: ContactsPageProps & WithTranslation) {
    const reloadContactsList = useRef(noop);

    const [addVisible, setAddVisible] = useState(initialShowAdd);
    const showAdd = useCallback(() => {
        setAddVisible(true);
        return AppRouter.updateQuery({[showAddParam]: true});
    }, []);
    const hideAdd = useCallback(() => {
        setAddVisible(false);
        return AppRouter.updateQuery({[showAddParam]: undefined});
    }, []);
    const onContactAdded = useCallback(() => {
        hideAdd();
        reloadContactsList.current();
    }, [hideAdd, reloadContactsList]);

    return (
        <MainLayout id='contacts' title={t('common:page.contacts')}>
            <div className='container'>
                <Heading
                    className='sm-t'
                    title={t('contacts:your_contacts')}
                    onAddContact={showAdd}
                />
                <section className='sm-t'>
                    <ContactsList onAddContact={showAdd} mutateRef={reloadContactsList}/>
                </section>
                <Modal title={t('common:actions.add_contact')} visible={addVisible} onCancel={hideAdd}>
                    <AddContactForm visible={addVisible} onComplete={onContactAdded}/>
                </Modal>
            </div>
        </MainLayout>
    );
}

ContactsPage.getInitialProps = (ctx: any) => {
    return {showAdd: ctx.query[showAddParam] === 'true'};
};

export default pageWithAuth({preAuthorize: 'isAuthenticated'})(pageWithTranslation('contacts')(ContactsPage));

export interface ContactsListProps {
    onAddContact: () => void;
    mutateRef: React.MutableRefObject<() => Promise<any>>;
}

const ContactsList = withAuth()(withTranslation('contacts')(function ContactsList({onAddContact, mutateRef, authGuard, t}: ContactsListProps & WithAuth & WithTranslation) {
    const authToken = authGuard.token;
    const catchAsyncError = useCatchAsyncError();

    const [tableQuery, setTableQuery] = useState({
        pageSize: 10,
        pageSort: undefined,
        page: 0,
    });
    const handleTableChange = useCallback((pagination, filters, sorter) => {
        setTableQuery((tableQuery) => ({
            ...tableQuery,
            pageSize: pagination.pageSize,
            pageSort: !sorter.order ? undefined : (sorter.order === 'descend' ? '-' : '') + sorter.field,
            page: pagination.current - 1,
        }));
    }, []);

    const {data: loadingData, error, mutate} = useSWR(
        [authToken, 'user/contact', tableQuery],
        (authToken, url, tableQuery) => apiClient
            .fetch<PageResponse<Contact>>({authToken, url: url + queryStringify(tableQuery)})
            .then((res) => {
                if (res.success === false) {
                    throw new UnhandledApiError(res.error);
                }
                return res.result;
            }),
        {refreshInterval: 60_000, errorRetryInterval: 15_000});
    mutateRef.current = mutate;
    const data = useStickyResult(loadingData);

    const deleteContact = useCallback((contact: Contact) => {
        return withNProgress(apiClient.fetch<void>({
            authToken,
            method: 'DELETE',
            url: 'user/contact/' + encodeURIComponent(contact.id),
        })).then((res): void | Promise<any> => {
            if (res.success === false) {
                if (res.error.code === 'CONTACT_NOT_FOUND') {
                    // Already deleted: reload the contact list without notification
                    return mutate();
                }
                throw new UnhandledApiError(res.error);
            }

            notification.success({message: t('contacts:removed', contact)});
            return mutate();
        }).catch(catchAsyncError);
    }, [authToken, catchAsyncError, mutate, t]);

    if (error) {
        return <InlineError error={error}/>;
    }

    const columns: ColumnsType<Contact> = [{
        dataIndex: 'name',
        title: t('contacts:name'),
        sorter: true,
    }, {
        dataIndex: 'email',
        title: t('contacts:email'),
        sorter: true,
    }, {
        key: 'action',
        align: 'right',
        render(i, contact) {
            return (
                <Popconfirm
                    title={t('contacts:remove_confirmation')}
                    onConfirm={doBindArgs(deleteContact, contact)}
                >
                    <Button>{t('contacts:remove')}</Button>
                </Popconfirm>
            );
        },
    }];
    return (
        <Card>
            {data && data.totalCount === 0 ? (
                <div className='text-center'>
                    <p>{t('contacts:no_contact_yet')}</p>
                    <Button type='primary' size='large' onClick={onAddContact}>
                        <Icon {...iconAddContact} marginRight={true}/>
                        {t('contacts:add_first_contact')}
                    </Button>
                </div>
            ) : (
                <div className='card-narrow-table'>
                    <Table
                        columns={columns}
                        rowKey='id'
                        dataSource={!data ? undefined : data.records}
                        loading={loadingData === undefined}
                        pagination={{
                            pageSize: tableQuery.pageSize,
                            current: tableQuery.page + 1,
                            total: !data ? undefined : data.totalCount,
                        }}
                        showSorterTooltip={false}
                        onChange={handleTableChange}
                    />
                </div>
            )}
        </Card>
    );
}));

interface AddContactFormProps {
    visible: boolean;
    onComplete: () => void;
}

const AddContactForm = withAuth()(withTranslation('contacts')(function AddContactForm({visible, onComplete, authGuard, t}: AddContactFormProps & WithAuth & WithTranslation) {
    const authToken = authGuard.token;
    const catchAsyncError = useCatchAsyncError();

    const [loading, setLoading] = useState(false);
    const [form] = useForm();
    const addContact = useCallback(({email}) => {
        setLoading(true);
        return apiClient.fetch<Contact>({
            authToken,
            url: 'user/contact',
            body: {email},
        }).then((res): void | Promise<any> => {
            if (res.success === false) {
                if (res.error.code === 'CANNOT_BE_HIMSELF') {
                    return setFormError(form, 'email', t('contacts:cannot_be_himself'));
                }
                if (res.error.code === 'CONTACT_NOT_FOUND') {
                    return setFormError(form, 'email', t('contacts:contact_not_found'));
                }
                throw new UnhandledApiError(res.error);
            }

            const contact = res.result;
            notification.success({message: t('contacts:added', contact)});
            return onComplete();
        }).catch(catchAsyncError).finally(() => setLoading(false));
    }, [authToken, onComplete, t, catchAsyncError, form]);

    return (
        <Spin spinning={loading}>
            <Form
                form={form}
                onFinish={addContact}
                layout='inline'
                className='flex-center'
            >
                <Form.Item
                    name='email'
                    label={t('contacts:email')}
                    validateTrigger='onBlur'
                    rules={[{required: true, pattern: /^[^@]+@[^@]+$/, message: t('contacts:require_email')}]}
                >
                    <Autofocus autoFocus={visible}>
                        <Input maxLength={255}/>
                    </Autofocus>
                </Form.Item>
                <Form.Item>
                    <Button type='primary' htmlType='submit'>
                        {t('contacts:add')}
                    </Button>
                </Form.Item>
            </Form>
        </Spin>
    );
}));
