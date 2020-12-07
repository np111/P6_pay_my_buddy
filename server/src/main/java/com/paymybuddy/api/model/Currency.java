package com.paymybuddy.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Currency {
    USD(2),
    EUR(2),
    JPY(0),
    GBP(2),
    CHF(2),
    CAD(2),
    AUD(2),
    HKD(2),
    ;

    private final int decimals;
}
