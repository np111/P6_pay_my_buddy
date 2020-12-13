type Currency = string;

export interface UserBalance {
    currency: Currency;
    amount: string;
}

export interface UserBalancesResponse {
    defaultCurrency: Currency;
    balances: UserBalance[];
}
