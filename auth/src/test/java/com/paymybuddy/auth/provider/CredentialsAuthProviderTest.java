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
class CredentialsAuthProviderTest {
    @Mock
    private AuthService authService;
    private CredentialsAuthProvider authProvider;

    @BeforeEach
    void setUp() {
        authProvider = new CredentialsAuthProvider(authService);
    }

    @Test
    void authenticate() {
        when(authService.login(any(), any())).thenAnswer(m -> {
            String email = m.getArgument(0);
            String password = m.getArgument(1);
            if ("EMAIL".equals(email) && "PASSWORD".equals(password)) {
                return AuthToken.authenticated(new MockAuthGuard(1L), "VALID");
            }
            throw new CredentialsExpiredException("");
        });

        Assertions.assertThrows(CredentialsExpiredException.class, () -> authProvider.authenticate(new UsernamePasswordAuthenticationToken("EMAIL", "INVALID")));
        Assertions.assertThrows(CredentialsExpiredException.class, () -> authProvider.authenticate(new UsernamePasswordAuthenticationToken(null, "INVALID")));
        AuthToken resultToken = authProvider.authenticate(new UsernamePasswordAuthenticationToken("EMAIL", "PASSWORD"));
        assertTrue(resultToken.isAuthenticated());
    }

    @Test
    void supports() {
        assertTrue(authProvider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(authProvider.supports(AuthToken.class));
    }
}