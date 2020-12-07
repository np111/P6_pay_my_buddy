package com.paymybuddy.server.http.auth;

import com.paymybuddy.api.model.user.User;

public interface AuthGuard {
    boolean isAuthenticated();

    long getUserId();

    User getUser();
}
