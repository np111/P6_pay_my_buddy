import React from 'react';
import {currencies} from '../../api/api-constants';
import {WithTranslation, withTranslation} from '../i18n';

export interface CurrencyAmountProps {
    currency: string;
    amount: string | number;
}

export const CurrencyAmount = withTranslation('common')(function CurrencyAmount({currency, amount, t}: CurrencyAmountProps & WithTranslation) {
    if (typeof amount === 'string') {
        amount = parseFloat(amount);
    }
    const currencyProps = currencies[currency];
    const amountStr = amount.toLocaleString('en-US', {
        useGrouping: true,
        minimumFractionDigits: currencyProps ? currencyProps.decimals : 0,
    }).replace(/[,.]/g, (v) => {
        return v === ',' ? t('common:intl.groupingSep', ',') : t('common:intl.decimalSep', '.');
    });
    return (
        <>
            {currencyProps
                ? currencyProps.prefix + amountStr + currencyProps.suffix
                : amountStr + ' ' + currency}
        </>
    );
});
