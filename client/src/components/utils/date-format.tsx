import moment, {Moment} from 'moment';
import {I18n, WithTranslation} from 'next-i18next';
import React from 'react';
import {withTranslation} from '../i18n';

// Date

export interface DateProps {
    date?: string | Moment;
    format?: string;
}

export function dateToMoment(date?: string | Moment) {
    if (typeof date === 'string') {
        return moment(date);
    }
    if (date) {
        return (date as Moment);
    }
    return moment();
}

export function dateFormat(props: DateProps, i18n: I18n) {
    let {date, format} = props;
    date = dateToMoment(date).locale(i18n.language);
    if (!format) {
        format = 'LL';
    }
    if (date.day() === 1 && (date.locale() === 'fr' || date.locale().startsWith('fr-'))) {
        switch (format) {
            case 'LL':
            case 'LLL':
            case 'LLLL':
                // Fix french ordinal - https://github.com/moment/moment/issues/4277
                format = date.localeData().longDateFormat(format).replace(/(^| )D( |$)/, '$1Do$2');
                break;
        }
    }
    return date.format(format);
}

export const DateFormat = withTranslation('common')(class Date extends React.PureComponent<DateProps & WithTranslation> {
    public render() {
        return dateFormat(this.props, this.props.i18n);
    }
});
