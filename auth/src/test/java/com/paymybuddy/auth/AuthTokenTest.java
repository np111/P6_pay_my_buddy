package com.paymybuddy.auth;

import com.paymybuddy.auth.mock.MockAuthGuard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenTest {
    private static final String UNAUTHENTICATED_TOKEN = "76a52910-a1e7-4a3e-b440-f3c53b441b8c";
    private static final String AUTHENTICATED_TOKEN = "8269ebcf-0351-422b-b3ad-4f36fd585862";

    @Test
    void unauthenticated() {
        AuthToken authToken = AuthToken.unauthenticated(UNAUTHENTICATED_TOKEN);
        assertFalse(authToken.isAuthenticated());
        assertEquals(UNAUTHENTICATED_TOKEN, authToken.getCredentials());
        assertNull(authToken.getPrincipal());

        authToken = AuthToken.unauthenticated(null);
        assertFalse(authToken.isAuthenticated());
        assertEquals("", authToken.getCredentials());
        assertNull(authToken.getPrincipal());
    }

    @Test
    void authenticated() {
        AuthGuard authGuard = new MockAuthGuard(1L);
        AuthToken authToken = AuthToken.authenticated(authGuard, AUTHENTICATED_TOKEN);
        assertTrue(authToken.isAuthenticated());
        assertEquals(AUTHENTICATED_TOKEN, authToken.getCredentials());
        assertEquals(authGuard, authToken.getPrincipal());

        Assertions.assertThrows(NullPointerException.class, () -> AuthToken.authenticated(null, AUTHENTICATED_TOKEN));
        Assertions.assertThrows(IllegalStateException.class, () -> AuthToken.authenticated(new MockAuthGuard(), AUTHENTICATED_TOKEN));
    }
}
