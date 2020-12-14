type Currency = string;

export interface UserBalance {
    currency: Currency;
    amount: string;
}

export interface UserBalancesResponse {
    defaultCurrency: Currency;
    balances: UserBalance[];
}

export interface Contact {
    id: number;
    email: string;
    name: string;
}

export interface Transaction {
    id: number;
    sender: Contact;
    recipient: Contact;
    currency: Currency;
    amount: string;
    fee: string;
    description: string;
    date: string;
}

export interface ListResponse<T> {
    records: T[];
}

export interface PageResponse<T> extends ListResponse<T> {
    page: number;
    pageSize: number;
    pageCount: number;
    totalCount: number;
}

export interface CursorResponse<T> extends ListResponse<T> {
    prevCursor: string;
    hasPrev: boolean;
    nextCursor: string;
    hasNext: boolean;
}
