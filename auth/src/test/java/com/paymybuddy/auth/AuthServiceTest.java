package com.paymybuddy.auth;

import com.paymybuddy.api.model.user.User;
import com.paymybuddy.auth.mock.TestAuthConfig;
import com.paymybuddy.auth.provider.UserProvider;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {AuthService.class})
@Import(TestAuthConfig.class)
class AuthServiceTest {
    @MockBean
    private UserProvider userProvider;
    @Autowired
    private AuthStore authStore;
    @Autowired
    @Qualifier("old-password-encoder")
    private PasswordEncoder oldPasswordEncoder;
    @Autowired
    private AuthService authService;

    @Test
    void loginAndRemember() {
        String email = "user@domain.tld";
        String password = UUID.randomUUID().toString();
        when(userProvider.getUserByEmail(email)).thenAnswer(m -> {
            User user = new User();
            user.setId(1L);
            user.setEmail(email);
            user.setEncodedPassword(oldPasswordEncoder.encode(password));
            return user;
        });

        // Valid login (then remember)
        AuthToken authToken = authService.login(email, password);
        assertTrue(authToken.isAuthenticated());
        assertEquals(email, authToken.getPrincipal().getUser().getEmail());
        AuthToken remember = authService.authenticateAuthToken(authToken.getCredentials());
        assertEquals(authToken, remember);

        // Invalid email
        assertThrows(BadCredentialsException.class, () -> authService.login("bad-email", password));
        // Invalid password
        assertThrows(BadCredentialsException.class, () -> authService.login(email, "bad-password"));
        // Invalid token (bad format)
        assertThrows(AuthenticationException.class, () -> authService.authenticateAuthToken("."));
        assertThrows(AuthenticationException.class, () -> authService.authenticateAuthToken(".x"));
        assertThrows(AuthenticationException.class, () -> authService.authenticateAuthToken("x."));
        assertThrows(AuthenticationException.class, () -> authService.authenticateAuthToken("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
        // Invalid token (expired)
        assertThrows(CredentialsExpiredException.class, () -> authService.authenticateAuthToken("2.84ad5cce-a11a-45c4-acc3-582602df71d5"));
    }

    @Test
    void destroyAuthToken() {
        AuthService.AuthGuardData data = new AuthService.AuthGuardData();
        data.setUserId(2L);
        authStore.save(data, "84ad5cce-a11a-45c4-acc3-582602df71d5", 30, TimeUnit.DAYS);

        authService.destroyAuthToken("xxx");
        assertNotNull(authStore.load("2", "84ad5cce-a11a-45c4-acc3-582602df71d5"));

        authService.destroyAuthToken("2.84ad5cce-a11a-45c4-acc3-582602df71d5");
        assertNull(authStore.load("2", "84ad5cce-a11a-45c4-acc3-582602df71d5"));
    }

    @Test
    void authGuard() {
        when(userProvider.getUserById(1L)).thenAnswer(m -> {
            User user = new User();
            user.setId(1L);
            return user;
        });

        AuthGuard unauthenticatedAuthGuard = authService.new AuthGuard();
        assertFalse(unauthenticatedAuthGuard.isAuthenticated());
        assertNull(unauthenticatedAuthGuard.getUser());

        AuthGuard authenticatedAuthGuard = authService.new AuthGuard(userProvider.getUserById(1L));
        assertTrue(authenticatedAuthGuard.isAuthenticated());
        assertEquals(userProvider.getUserById(1L), authenticatedAuthGuard.getUser());

        for (long id = 1L; id <= 2L; ++id) {
            AuthService.AuthGuardData data = new AuthService.AuthGuardData();
            data.setUserId(id);
            AuthGuard lazyAuthGuard = authService.new AuthGuard(data);
            assertTrue(lazyAuthGuard.isAuthenticated());
            if (id == 1L) {
                assertEquals(userProvider.getUserById(1L), lazyAuthGuard.getUser());
            } else {
                assertThrows(RuntimeException.class, lazyAuthGuard::getUser);
            }
        }
    }
}
