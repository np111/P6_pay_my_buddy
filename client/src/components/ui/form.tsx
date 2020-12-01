import AntForm, {FormInstance as AntFormInstance, FormProps as AntFormProps} from 'antd/lib/form';
import {useForm as antUseForm} from 'antd/lib/form/Form';
import 'antd/lib/form/style/index.less';

export type FormProps = AntFormProps;
export const Form = AntForm;

export type FormInstance = AntFormInstance;
export const useForm = antUseForm;
