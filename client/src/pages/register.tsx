import React, {useCallback, useState} from 'react';
import {apiClient} from '../api/api-client';
import {currenciesOptions} from '../api/api-constants';
import {UnhandledApiError} from '../api/api-exception';
import '../assets/css/pages/register.scss';
import {pageWithAuth, WithAuth} from '../components/auth/with-auth';
import {pageWithTranslation, withTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';
import {Button} from '../components/ui/button';
import {Card} from '../components/ui/card';
import {Form, useForm} from '../components/ui/form';
import {Input} from '../components/ui/input';
import {Select} from '../components/ui/select';
import {Spin} from '../components/ui/spin';
import {Autofocus} from '../components/utils/autofocus';
import {setFormError} from '../utils/form-utils';
import notification from '../utils/notification';
import {useCatchAsyncError} from '../utils/react-utils';
import {AppRouter, routes} from '../utils/routes';

function RegisterPage({t}: WithAuth & WithTranslation) {
    return (
        <MainLayout id='register' title={t('common:page.register')}>
            <div className='container sm-t'>
                <Card className='register-card'>
                    <RegisterForm/>
                </Card>
            </div>
        </MainLayout>
    );
}

export default pageWithAuth({preAuthorize: 'isAnonymous'})(pageWithTranslation('register')(RegisterPage));

const RegisterForm = withTranslation()(function ({t}: WithTranslation) {
    const catchAsyncError = useCatchAsyncError();
    const [loading, setLoading] = useState(false);
    const [form] = useForm();
    const register = useCallback(({name, email, password, defaultCurrency}) => {
        setLoading(true);
        return apiClient.fetch({
            authToken: false,
            url: 'auth/register',
            body: {
                name,
                email,
                password,
                defaultCurrency,
            },
        }).then((res): void | Promise<any> => {
            if (res.success === false) {
                if (res.error.code === 'INVALID_NAME') {
                    return setFormError(form, 'name', t('register:invalid_name'));
                }
                if (res.error.code === 'INVALID_EMAIL') {
                    return setFormError(form, 'email', res.error.metadata.alreadyExists
                        ? t('register:email_already_registered')
                        : t('register:invalid_email'));
                }
                if (res.error.code === 'INVALID_PASSWORD') {
                    return setFormError(form, 'email', t('register:invalid_password'));
                }
                throw new UnhandledApiError(res.error);
            }

            notification.success({message: t('register:registered')});
            return AppRouter.push(routes.login());
        }).catch(catchAsyncError).finally(() => setLoading(false));
    }, [t, catchAsyncError, form]);

    return (
        <Spin spinning={loading}>
            <Form
                {...layout}
                form={form}
                onFinish={register}
            >
                <Form.Item
                    name='name'
                    label={t('register:name')}
                    validateTrigger='onBlur'
                    rules={[{required: true, message: t('register:require_name')}]}
                >
                    <Autofocus>
                        <Input maxLength={255}/>
                    </Autofocus>
                </Form.Item>
                <Form.Item
                    name='email'
                    label={t('register:email')}
                    validateTrigger='onBlur'
                    rules={[{required: true, pattern: /^[^@]+@[^@]+$/, message: t('register:require_email')}]}
                >
                    <Input maxLength={255}/>
                </Form.Item>
                <Form.Item
                    name='password'
                    label={t('register:password')}
                    validateTrigger='onBlur'
                    rules={[{required: true, message: t('register:require_password')}]}
                >
                    <Input.Password maxLength={50}/>
                </Form.Item>
                <Form.Item
                    name='passwordConfirmation'
                    dependencies={['password']}
                    label={t('register:password_confirmation')}
                    validateTrigger='onBlur'
                    rules={[
                        ({getFieldValue}) => ({
                            required: true,
                            validator(rule, value) {
                                return getFieldValue('password') === value
                                    ? Promise.resolve()
                                    : Promise.reject(t('register:invalid_password_confirmation'));
                            },
                        }),
                    ]}
                >
                    <Input.Password maxLength={255}/>
                </Form.Item>
                <Form.Item
                    name='defaultCurrency'
                    label={t('register:default_currency')}
                    initialValue={'EUR'}
                >
                    <Select options={currenciesOptions}/>
                </Form.Item>
                <Form.Item {...tailLayout}>
                    <Button type='primary' htmlType='submit' size='large'>
                        {t('register:register')}
                    </Button>
                </Form.Item>
            </Form>
        </Spin>
    );
});
const layout = {
    labelCol: {sm: 8},
    wrapperCol: {sm: 16},
};
const tailLayout = {
    wrapperCol: {sm: {offset: 8, span: 16}},
};
