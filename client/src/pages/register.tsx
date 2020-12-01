import Button from 'antd/lib/button';
import Form from 'antd/lib/form';
import {useForm} from 'antd/lib/form/Form';
import Input from 'antd/lib/input';
import Skeleton from 'antd/lib/skeleton';
import Spin from 'antd/lib/spin';
import React, {useState} from 'react';
import {UnhandledApiError} from '../api/api-exception';
import {apiFetch} from '../api/api-fetch';
import {WithAuth, withAuth} from '../components/auth/with-auth';
import {pageWithTranslation, withTranslation, WithTranslation} from '../components/i18n';
import {MainLayout} from '../components/layout/main-layout';
import {Autofocus} from '../components/utils/autofocus';
import {setFormError} from '../utils/form-utils';
import {useCatchAsyncError} from '../utils/react-utils';
import {AppRouter, routes} from '../utils/routes';

export default withAuth()(pageWithTranslation('register')(function Login({t, authMethods, authGuard}: WithAuth & WithTranslation) {
    return (
        <MainLayout id='register' title={t('common:page.register')}>
            <div className='container sm-t'>
                {!authGuard.authenticated ? (
                    <RegisterForm/>
                ) : (
                    <>TODO: Redirect to account</>
                )}
            </div>
        </MainLayout>
    );
}));

const layout = {
    labelCol: {
        sm: {span: 8},
        lg: {span: 8},
    },
    wrapperCol: {
        sm: {span: 16},
        lg: {span: 12},
    },
};
const tailLayout = {
    wrapperCol: {
        sm: {offset: 8, span: 16},
    },
};
// TODO: i18n
// TODO: select default currency
const RegisterForm = withAuth()(withTranslation()(function ({t, authenticating}: WithAuth & WithTranslation) {
    const catchAsyncError = useCatchAsyncError();
    const [loading, setLoading] = useState(false);
    const [form] = useForm();
    const register = ({name, email, password}) => {
        console.log({name, email, password});
        setLoading(true);
        return apiFetch({
            authToken: false,
            url: 'auth/register',
            body: {
                name,
                email,
                password,
                defaultCurrency: 'EUR',
            },
        }).then((res): void | Promise<any> => {
            if (res.success == false) {
                if (res.error.code === 'EMAIL_ALREADY_EXISTS') {
                    return setFormError(form, 'email', t('common:register.email_already_registered'));
                }
                // TODO: catch all errors
                throw new UnhandledApiError(res.error);
            }
            // TODO: display a confirmation notification
            return AppRouter.push(routes.login());
        }).catch(catchAsyncError).finally(() => setLoading(false));
    };
    if (authenticating) {
        return <Skeleton loading={true}/>;
    }
    return (
        <Spin spinning={loading}>
            <Form
                {...layout}
                form={form}
                onFinish={register}
            >
                <Form.Item
                    name='name'
                    label={t('common:register.name')}
                    validateTrigger='onBlur'
                    rules={[{required: true, message: t('common:register.require_name')}]}
                >
                    <Autofocus>
                        <Input maxLength={255}/>
                    </Autofocus>
                </Form.Item>
                <Form.Item
                    name='email'
                    label={t('common:register.email')}
                    validateTrigger='onBlur'
                    rules={[{required: true, pattern: /^[^@]+@[^@]+$/, message: t('common:register.require_email')}]}
                >
                    <Input maxLength={255}/>
                </Form.Item>
                <Form.Item
                    name='password'
                    label={t('common:register.password')}
                    validateTrigger='onBlur'
                    rules={[{required: true, message: t('common:register.require_password')}]}
                >
                    <Input.Password maxLength={255}/>
                </Form.Item>
                <Form.Item
                    name='passwordConfirmation'
                    dependencies={['password']}
                    label={t('common:register.password_confirmation')}
                    validateTrigger='onBlur'
                    rules={[
                        ({getFieldValue}) => ({
                            required: true,
                            validator(rule, value) {
                                return getFieldValue('password') === value
                                    ? Promise.resolve()
                                    : Promise.reject(t('common:register.invalid_password_confirmation'));
                            },
                        }),
                    ]}
                >
                    <Input.Password maxLength={255}/>
                </Form.Item>
                <Form.Item {...tailLayout}>
                    <Button type='primary' htmlType='submit' size='large'>
                        {t('common:auth.register')}
                    </Button>
                </Form.Item>
            </Form>
        </Spin>
    );
}));
