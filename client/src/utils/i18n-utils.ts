import {TFunction} from 'next-i18next';

export function titleSep(t: TFunction, ...args: any[]) {
    const title = t.apply(undefined, args);
    return t('common:intl.titleSep', title, {title});
}
