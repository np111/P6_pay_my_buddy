package com.paymybuddy.api.model.transaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.api.util.jackson.AmountSerializer;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class Transaction {
    private Long id;

    private User sender;

    private User recipient;

    private Currency currency;

    @JsonSerialize(using = AmountSerializer.class)
    private BigDecimal amount;

    @JsonSerialize(using = AmountSerializer.class)
    private BigDecimal fee;

    private String description;

    private ZonedDateTime date;
}
