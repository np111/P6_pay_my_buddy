package com.paymybuddy.auth;

import com.google.common.base.Preconditions;
import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Authentication token, associated with an {@link AuthGuard} when the token is authenticated.
 */
public final class AuthToken extends AbstractAuthenticationToken {
    public static AuthToken unauthenticated(String token) {
        return new AuthToken(null, token);
    }

    public static AuthToken authenticated(AuthGuard auth, String token) {
        Preconditions.checkNotNull(auth, "auth cannot be null");
        Preconditions.checkState(auth.isAuthenticated());
        return new AuthToken(auth, token);
    }

    private final AuthGuard auth;
    private final String token;

    private AuthToken(AuthGuard auth, String token) {
        super(auth == null ? null : Collections.emptyList()); // (no authorities)
        this.auth = auth;
        this.token = token == null ? "" : token;
        setAuthenticated(auth != null);
    }

    @Override
    public AuthGuard getPrincipal() {
        return auth;
    }

    @Override
    public String getCredentials() {
        return token;
    }
}
