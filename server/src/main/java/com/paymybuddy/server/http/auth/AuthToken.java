package com.paymybuddy.server.http.auth;

import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class AuthToken extends AbstractAuthenticationToken {
    private final Auth auth;
    private final String token;

    public AuthToken(String token) {
        super(null);
        this.auth = null;
        this.token = token;
    }

    public AuthToken(Auth auth, String token) {
        super(Collections.emptyList()); // (no authorities)
        this.auth = auth;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Auth getPrincipal() {
        return auth;
    }

    @Override
    public String getCredentials() {
        return token;
    }
}
