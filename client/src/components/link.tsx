import {LinkProps as NextLinkProps} from 'next/link';
import React from 'react';
import {i18nNextInstance} from '../i18n-instance';

const I18nNextLink = i18nNextInstance.Link;

export type LinkProps = NextLinkProps;

export class Link extends React.Component<LinkProps> {
    render() {
        const {children} = this.props;
        let {passHref} = this.props;
        if (passHref === undefined && isButton(children)) {
            passHref = true;
        }
        return <I18nNextLink {...this.props} passHref={passHref}/>;
    }
}

function isButton(children: any) {
    return !!(children && children.type && children.type.__ANT_BUTTON);
}
