package com.paymybuddy.auth.mock;

import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.auth.AuthGuard;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class MockAuthGuard implements AuthGuard {
    private static User createMockUser(long userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail(userId + "@domain.tld");
        user.setName("#" + userId);
        user.setDefaultCurrency(Currency.USD);
        user.setEncodedPassword("");
        return user;
    }

    private final boolean authenticated;
    private final long userId;
    private final User user;

    public MockAuthGuard() {
        this.authenticated = false;
        this.userId = 0L;
        this.user = null;
    }

    public MockAuthGuard(long userId) {
        this(userId, createMockUser(userId));
    }

    public MockAuthGuard(long userId, User user) {
        this.authenticated = true;
        this.userId = userId;
        this.user = user;
    }
}
