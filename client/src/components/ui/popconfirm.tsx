import AntPopconfirm, {PopconfirmProps as AntPopconfirmProps} from 'antd/lib/popconfirm';
import 'antd/lib/popconfirm/style/index.less';
import 'antd/lib/popover/style/index.less';

export type PopconfirmProps = AntPopconfirmProps;
export const Popconfirm = AntPopconfirm; // TODO: Default okText/cancelText based on locale
