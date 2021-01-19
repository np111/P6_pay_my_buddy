package com.paymybuddy.api.request.transaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.util.jackson.AmountSerializer;
import com.paymybuddy.api.util.validation.constraint.IsAmount;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@IsAmount(amountField = "amount", currencyField = "currency", strictlyPositive = true)
public class WithdrawToBankRequest {
    @NonNull
    private Currency currency;

    @NonNull
    @JsonSerialize(using = AmountSerializer.class)
    private BigDecimal amount;

    @NonNull
    private String iban;
}
