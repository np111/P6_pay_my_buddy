import {FormInstance} from '../components/ui/form';

export function setFormError(form: FormInstance, fieldName: string, error: string) {
    form.setFields([{name: fieldName, errors: [error]}]);
}
