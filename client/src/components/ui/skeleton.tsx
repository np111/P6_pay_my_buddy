import AntSkeleton, {SkeletonProps as AntSkeletonProps} from 'antd/lib/skeleton';
import {SkeletonButtonProps as AntSkeletonButtonProps} from 'antd/lib/skeleton/Button';
import 'antd/lib/skeleton/style/index.less';
import React from 'react';

export type SkeletonProps = AntSkeletonProps;

export function Skeleton(props: SkeletonProps) {
    return <AntSkeleton active={true} {...props}/>;
}

export type SkeletonButtonProps = AntSkeletonButtonProps;

Skeleton.Button = function SkeletonButton(props: SkeletonButtonProps) {
    return <AntSkeleton.Button active={true} {...props}/>;
};
