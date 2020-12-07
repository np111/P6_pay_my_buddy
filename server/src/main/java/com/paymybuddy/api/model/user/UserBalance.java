package com.paymybuddy.api.model.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.util.jackson.AmountSerializer;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class UserBalance {
    private Currency currency;

    @JsonSerialize(using = AmountSerializer.class)
    private BigDecimal amount;
}
