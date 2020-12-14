import AntSelect, {SelectProps as AntdSelectProps} from 'antd/lib/select';
import 'antd/lib/select/style/index.less';

export type SelectProps<VT> = AntdSelectProps<VT>;
export const Select = AntSelect;
