import AntInput, {InputProps as AntInputProps, TextAreaProps as AntTextAreaProps} from 'antd/lib/input';
import 'antd/lib/input/style/index.less';

export type InputProps = AntInputProps;
export const Input = AntInput;

export type TextAreaProps = AntTextAreaProps;
export const TextArea = AntInput.TextArea;
