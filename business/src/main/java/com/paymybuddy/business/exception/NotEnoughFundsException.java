package com.paymybuddy.business.exception;

import com.paymybuddy.api.model.Currency;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class NotEnoughFundsException extends FastRuntimeException {
    private final Currency currency;
    private final BigDecimal missingAmount;

    public NotEnoughFundsException(Currency currency, BigDecimal missingAmount) {
        this.currency = currency;
        this.missingAmount = missingAmount;
    }
}
