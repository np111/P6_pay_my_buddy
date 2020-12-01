import {FormInstance} from 'antd/lib/form';

export function setFormError(form: FormInstance, fieldName: string, error: string) {
    form.setFields([{name: fieldName, errors: [error]}]);
}
