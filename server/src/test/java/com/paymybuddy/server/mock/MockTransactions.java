package com.paymybuddy.server.mock;

import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.transaction.Transaction;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MockTransactions {
    public static Transaction newTransaction(long id) {
        return Transaction.builder()
                .id(id)
                .sender(MockUsers.newUser(1L))
                .recipient(MockUsers.newUser(2L))
                .currency(Currency.USD)
                .amount(new BigDecimal(id * 33L))
                .fee(new BigDecimal("0.01"))
                .description("Desc" + id)
                .date(ZonedDateTime.of(2021, 1, 18, 0, 36, 30, 0, ZoneOffset.UTC))
                .build();
    }
}
