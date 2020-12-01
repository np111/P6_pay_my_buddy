import Button from 'antd/lib/button';
import Form from 'antd/lib/form';
import {useForm} from 'antd/lib/form/Form';
import Input from 'antd/lib/input';
import Skeleton from 'antd/lib/skeleton';
import Spin from 'antd/lib/spin';
import React, {useState} from 'react';
import {setFormError} from '../../utils/form-utils';
import {useCatchAsyncError} from '../../utils/react-utils';
import {AppRouter, routes} from '../../utils/routes';
import {WithTranslation, withTranslation} from '../i18n';
import {Link, LinkProps} from '../link';
import {Autofocus} from '../utils/autofocus';
import {WithAuth, withAuth} from './with-auth';

export interface LoginFormProps {
    redirect?: LinkProps;
}

export const LoginForm = withAuth()(withTranslation('common')(function ({t, authenticating, authGuard, redirect}: LoginFormProps & WithAuth & WithTranslation) {
    const catchAsyncError = useCatchAsyncError();
    const [loading, setLoading] = useState(false);
    const [form] = useForm();
    const login = ({email, password}) => {
        setLoading(true);
        return authGuard
            .login(email, password)
            .then((logged) => {
                if (!logged) {
                    setFormError(form, 'email', t('common:auth.invalid_credentials'));
                } else if (redirect) {
                    return AppRouter.push(redirect);
                }
            })
            .catch(catchAsyncError)
            .finally(() => setLoading(false));
    };
    if (authenticating) {
        return <Skeleton loading={true}/>;
    }
    if (authGuard.authenticated) {
        return null;
    }
    return (
        <Spin spinning={loading}>
            <div className='login-form'>
                <Form
                    form={form}
                    onFinish={login}
                >
                    <Form.Item
                        name='email'
                        validateTrigger='onBlur'
                        rules={[{required: true, pattern: /^[^@]+@[^@]+$/, message: t('common:auth.require_email')}]}
                    >
                        <Autofocus>
                            <Input placeholder={t('common:auth.email')} maxLength={255} size='large'/>
                        </Autofocus>
                    </Form.Item>
                    <Form.Item
                        name='password'
                        validateTrigger='onBlur'
                        rules={[{required: true, message: t('common:auth.require_password')}]}
                    >
                        <Input.Password placeholder={t('common:auth.password')} maxLength={255} size='large'/>
                    </Form.Item>
                    <Form.Item>
                        <Button type='primary' htmlType='submit' size='large' block={true}>
                            {t('common:auth.login')}
                        </Button>
                    </Form.Item>
                </Form>
                <div className='separator'><span>{t('common:auth.or')}</span></div>
                <Link {...routes.register()}>
                    <Button size='large' block={true}>{t('common:page.register')}</Button>
                </Link>
            </div>
        </Spin>
    );
}));
