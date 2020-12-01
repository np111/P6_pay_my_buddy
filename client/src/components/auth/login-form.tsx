import Button from 'antd/lib/button';
import Form from 'antd/lib/form';
import {useForm} from 'antd/lib/form/Form';
import Input from 'antd/lib/input';
import Skeleton from 'antd/lib/skeleton';
import Spin from 'antd/lib/spin';
import React, {useState} from 'react';
import {setFormError} from '../../utils/form-utils';
import {useCatchAsyncError} from '../../utils/react-utils';
import {WithTranslation, withTranslation} from '../i18n';
import {WithAuth, withAuth} from './with-auth';

const layout = {
    labelCol: {span: 8},
    wrapperCol: {span: 16},
};
const tailLayout = {
    wrapperCol: {xs: 24, sm: {offset: 8, span: 16}},
};

// TODO: i18n
export const LoginForm = withAuth()(withTranslation('common')(function ({t, authenticating, authMethods, authGuard}: WithAuth & WithTranslation) {
    const catchAsyncError = useCatchAsyncError();
    const [loading, setLoading] = useState(false);
    const [form] = useForm();
    const login = ({email, password}) => {
        setLoading(true);
        return authMethods
            .login(email, password)
            .then((logged) => {
                if (!logged) {
                    setFormError(form, 'email', 'Invalid credentials');
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
            <Form
                {...layout}
                form={form}
                onFinish={login}
            >
                <Form.Item
                    label='Email'
                    name='email'
                    validateTrigger='onBlur'
                    rules={[{required: true, pattern: /^[^@]+@[^@]+$/, message: 'Please input your email!'}]}
                >
                    <Input/>
                </Form.Item>
                <Form.Item
                    label='Password'
                    name='password'
                    validateTrigger='onBlur'
                    rules={[{required: true, message: 'Please input your password!'}]}
                >
                    <Input.Password/>
                </Form.Item>
                <Form.Item {...tailLayout}>
                    <Button type='primary' htmlType='submit'>
                        Login
                    </Button>
                </Form.Item>
            </Form>
        </Spin>
    );
}));
