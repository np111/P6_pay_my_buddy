import ConfigProvider from 'antd/lib/config-provider';
import {RenderEmptyHandler} from 'antd/lib/config-provider/renderEmpty';
import 'antd/lib/pagination/style/index.less';
import 'antd/lib/select/style/index.less';
import 'antd/lib/spin/style/index.less';
import AntTable, {TableProps as AntTableProps} from 'antd/lib/table';
import {ColumnsType as AntColumnsType} from 'antd/lib/table/interface';
import 'antd/lib/table/style/index.less';
import 'antd/lib/tooltip/style/index.less';
import {ExpandableConfig as AntExpandableConfig} from 'rc-table/lib/interface';
import React from 'react';

export type TableProps<RecordType> = AntTableProps<RecordType> & {
    renderEmpty?: RenderEmptyHandler;
};
export type ColumnsType<RecordType> = AntColumnsType<RecordType>;
export type ExpandableConfig<RecordType> = AntExpandableConfig<RecordType>;

// eslint-disable-next-line
export function Table<RecordType extends object = any>(props: TableProps<RecordType>) {
    return (
        <ConfigProvider renderEmpty={props.renderEmpty || defaultRenderEmpty}>
            <AntTable {...props}/>
        </ConfigProvider>
    );
}

const defaultRenderEmpty = () => null;
