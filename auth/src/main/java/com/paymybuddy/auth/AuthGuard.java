package com.paymybuddy.auth;

import com.paymybuddy.api.model.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Our {@link AuthenticationPrincipal authentication principal}, allowing to access the current authentication state.
 */
public interface AuthGuard {
    boolean isAuthenticated();

    long getUserId();

    User getUser();
}
