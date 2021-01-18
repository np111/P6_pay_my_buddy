package com.paymybuddy.server.mock;

import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.user.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MockUsers {
    public static User newUser(long userId) {
        return User.builder()
                .id(userId)
                .email(userId + "@domain.tld")
                .name("#" + userId)
                .defaultCurrency(Currency.USD)
                .build();
    }

    public static User newContact(long userId) {
        return newUser(userId);
    }
}
