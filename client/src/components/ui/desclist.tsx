import React, {HTMLAttributes} from 'react';
import '../../assets/css/components/desclist.scss';

export interface DescListProps extends React.DetailedHTMLProps<HTMLAttributes<HTMLDivElement>, HTMLDivElement> {
}

export class DescList extends React.Component<DescListProps> {
    public render() {
        const {className, ...props} = this.props;
        return <div className={className ? 'dl ' + className : 'dl'} {...props}/>;
    }
}

export interface DescListItemProps {
    title?: React.ReactNode;
    value?: React.ReactNode;
}

export class DescListItem extends React.Component<DescListItemProps> {
    public render() {
        const {title, value} = this.props;
        return (
            <div className='dl-item'>
                {title === undefined ? undefined : <div className='dl-title'>{title}</div>}
                {value === undefined ? undefined : <div className='dl-value'>{value}</div>}
            </div>
        );
    }
}
