package com.paymybuddy.auth.provider;

import com.paymybuddy.auth.AuthService;
import com.paymybuddy.auth.AuthToken;
import com.paymybuddy.auth.mock.MockAuthGuard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthProviderTest {
    @Mock
    private AuthService authService;
    private TokenAuthProvider authProvider;

    @BeforeEach
    void setUp() {
        authProvider = new TokenAuthProvider(authService);
    }

    @Test
    void authenticate() {
        when(authService.authenticateAuthToken(any())).thenAnswer(m -> {
            String token = m.getArgument(0);
            if ("VALID".equals(token)) {
                return AuthToken.authenticated(new MockAuthGuard(1L), token);
            }
            throw new CredentialsExpiredException("");
        });

        Assertions.assertThrows(CredentialsExpiredException.class, () -> authProvider.authenticate(AuthToken.unauthenticated("INVALID")));
        AuthToken resultToken = authProvider.authenticate(AuthToken.unauthenticated("VALID"));
        assertTrue(resultToken.isAuthenticated());
    }

    @Test
    void supports() {
        assertTrue(authProvider.supports(AuthToken.class));
        assertFalse(authProvider.supports(UsernamePasswordAuthenticationToken.class));
    }
}