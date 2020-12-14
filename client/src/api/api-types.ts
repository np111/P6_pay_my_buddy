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

export interface AutocompleteContactResponse {
    records: Contact[];
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
