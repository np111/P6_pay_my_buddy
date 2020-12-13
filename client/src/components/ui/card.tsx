import classNames from 'classnames';
import React from 'react';
import '../../assets/css/components/card.scss';

export type CardProps = {
    title?: React.ReactNode;
    children?: React.ReactNode;
} & Omit<React.HTMLAttributes<any>, 'title'>;

export function Card({title, children, ...divProps}: CardProps) {
    return (
        <div {...divProps} className={classNames('card', divProps.className)}>
            {title === undefined ? undefined : (
                <div className='title'>{title}</div>
            )}
            {children === undefined ? undefined : (
                <div className='content'>{children}</div>
            )}
        </div>
    );
}
