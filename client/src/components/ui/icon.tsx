import {FontAwesomeIcon, FontAwesomeIconProps} from '@fortawesome/react-fontawesome';
import React from 'react';

export interface IconProps extends FontAwesomeIconProps {
    marginLeft?: boolean;
    marginRight?: boolean;
}

export class Icon extends React.Component<IconProps> {
    public render() {
        const {marginLeft, marginRight, ...props} = this.props;
        const style: React.CSSProperties = {...props.style};
        if (marginLeft) {
            style.marginLeft = '0.5em';
        }
        if (marginRight) {
            style.marginRight = '0.5em';
        }
        return <FontAwesomeIcon {...props} style={style}/>;
    }
}
