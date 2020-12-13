import {Col as AntCol, ColProps as AntColProps, Row as AntRow, RowProps as AntRowProps} from 'antd/lib/grid';
import 'antd/lib/grid/style/index.less';
import React from 'react';

const sectionMarginPx = 24; // $section-margin

export type ColProps = AntColProps;
export const Col = AntCol;

export type RowProps = AntRowProps;

export function Row(props: RowProps) {
    return <AntRow {...props} gutter={props.gutter === undefined ? sectionMarginPx : props.gutter}/>;
}
