package com.paymybuddy.business.mock;

import com.paymybuddy.api.model.Currency;
import com.paymybuddy.persistence.entity.UserBalanceEntity;
import com.paymybuddy.persistence.entity.UserEntity;
import java.math.BigDecimal;
import java.util.ArrayList;

public class MockUsers {
    public static UserEntity newUserEntity(long id) {
        UserEntity e = new UserEntity();
        e.setId(id);
        e.setEmail(id + "@domain.tld");
        e.setName("#" + id);
        e.setDefaultCurrency(Currency.USD);
        e.setEncodedPassword("");
        e.setBalances(new ArrayList<>());
        return e;
    }

    public static UserBalanceEntity newBalance(BigDecimal amount, Currency currency) {
        return newBalance(amount, currency, null);
    }

    public static UserBalanceEntity newBalance(BigDecimal amount, Currency currency, UserEntity user) {
        UserBalanceEntity balanceEntity = new UserBalanceEntity();
        balanceEntity.setAmount(amount);
        balanceEntity.setCurrency(currency);
        if (user != null) {
            balanceEntity.setUser(user);
            balanceEntity.setUserId(user.getId());
        }
        return balanceEntity;
    }
}
