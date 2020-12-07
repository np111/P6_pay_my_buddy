import React, {useCallback, useState} from 'react';
import {setFormError} from '../../utils/form-utils';
import {useCatchAsyncError} from '../../utils/react-utils';
import {AppRouter, routes} from '../../utils/routes';
import {Link, LinkProps, WithTranslation, withTranslation} from '../i18n';
import {Button} from '../ui/button';
import {Form, useForm} from '../ui/form';
import {Input} from '../ui/input';
import {Spin} from '../ui/spin';
import {Autofocus} from '../utils/autofocus';
import {WithAuth, withAuth} from './with-auth';

export interface LoginFormProps {
    redirect?: LinkProps;
}

export const LoginForm = withAuth()(withTranslation('common')(function ({t, authMethods, redirect}: LoginFormProps & WithAuth & WithTranslation) {
    const catchAsyncError = useCatchAsyncError();
    const [loading, setLoading] = useState(false);
    const [form] = useForm();
    const login = useCallback(({email, password}) => {
        setLoading(true);
        return authMethods
            .login(email, password)
            .then((logged) => {
                if (!logged) {
                    setLoading(false);
                    setFormError(form, 'email', t('common:auth.invalid_credentials'));
                } else if (redirect) {
                    return AppRouter.push(redirect);
                }
            })
            .catch((err) => {
                setLoading(false);
                catchAsyncError(err);
            });
    }, [t, authMethods, catchAsyncError, form, redirect]);
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
