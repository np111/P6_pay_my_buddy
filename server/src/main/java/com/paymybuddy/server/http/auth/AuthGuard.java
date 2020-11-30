package com.paymybuddy.server.http.auth;

import com.paymybuddy.api.model.User;

public interface AuthGuard {
    boolean isAuthenticated();

    long getUserId();

    User getUser();
}
