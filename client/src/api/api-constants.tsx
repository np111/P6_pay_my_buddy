export interface CurrencyProps {
    decimals: number;
    prefix: string;
    suffix: string;
}

export const currencies: { [currency: string]: CurrencyProps } = {
    USD: {decimals: 2, prefix: '$', suffix: ' USD'},
    EUR: {decimals: 2, prefix: '', suffix: '€'},
    JPY: {decimals: 0, prefix: '¥', suffix: ''},
    GBP: {decimals: 2, prefix: '£', suffix: ''},
    CHF: {decimals: 2, prefix: '', suffix: ' CHF'},
    CAD: {decimals: 2, prefix: '$', suffix: ' CAD'},
    AUD: {decimals: 2, prefix: '$', suffix: ' AUD'},
    HKD: {decimals: 2, prefix: 'HK$ ', suffix: ''},
};

export const currenciesOptions = Object.keys(currencies).sort().map((currency) => ({value: currency}));
