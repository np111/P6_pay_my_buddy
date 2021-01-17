package com.paymybuddy.auth;

import com.paymybuddy.api.model.user.User;
import com.paymybuddy.auth.provider.UserProvider;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication management service.
 * <p>
 * This service use a {@link UserProvider} to login using email/password credentials.
 * It then creates an authentication session, stored in redis, and associates it a secret token.
 * Afterward this secret token can be used to login.
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class AuthService implements InitializingBean {
    /**
     * Max-length of a token string (used for fast-fail checks).
     * Token is composed of a positive long (as base10 string) and an UUID, joined by a dash.
     */
    private static final int AUTH_TOKEN_MAX_LEN = Long.toString(Long.MAX_VALUE).length() + 1 + new UUID(0, 0).toString().length();

    private final UserProvider userProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthStore authStore;
    private String userNotFoundEncodedPassword;

    @Override
    public void afterPropertiesSet() {
        userNotFoundEncodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /**
     * Authenticate using email and password.
     *
     * @param email    email of the user to authenticate
     * @param password password to check
     * @return the created {@link AuthToken}
     * @throws BadCredentialsException if no users exists with that email or the password is invalid
     */
    public AuthToken login(String email, String password) {
        User user = userProvider.getUserByEmail(email);
        if (user == null) {
            // Mitigate timing attack.
            // Note: This is not really needed since anyone can easily check if an user exists by email (eg. when registering).
            passwordEncoder.matches(password, userNotFoundEncodedPassword);
            throw new BadCredentialsException("Bad credentials");
        }
        if (!passwordEncoder.matches(password, user.getEncodedPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }
        if (passwordEncoder.upgradeEncoding(user.getEncodedPassword())) {
            // Upgrade password encoding
            userProvider.updateEncodedPassword(user, passwordEncoder.encode(password));
        }
        return createAuthToken(user);
    }

    /**
     * Create a new authentication session for an user.
     *
     * @param user authenticated user
     * @return the created auth token
     */
    private AuthToken createAuthToken(User user) {
        AuthGuard auth = new AuthGuard(user);

        // Generate a secure-random session ID and derive a token from it
        String sessionId = UUID.randomUUID().toString();
        String token = encodeToken(user.getId(), sessionId);

        // Store the session in redis (in a per-user hash, which allows us to easily list or delete all of a user's sessions)
        authStore.save(auth.toData(), sessionId, 30, TimeUnit.DAYS);

        return AuthToken.authenticated(auth, token);
    }

    /**
     * Resume an authentication session by it's secret token.
     *
     * @param token secret token
     * @return the resumed auth token
     * @throws CredentialsExpiredException if the token is invalid or has expired
     */
    @Nullable
    public AuthToken authenticateAuthToken(String token) {
        // Extract userId and sessionId from token
        Pair<String, String> tokenPair = decodeToken(token);
        if (tokenPair == null) {
            throw new CredentialsExpiredException("Invalid or expired auth token");
        }
        String userId = tokenPair.getFirst();
        String sessionId = tokenPair.getSecond();

        // Load the session from redis
        AuthGuardData authData = authStore.load(userId, sessionId);
        if (authData == null) {
            throw new CredentialsExpiredException("Invalid or expired auth token");
        }

        return AuthToken.authenticated(new AuthGuard(authData), token);
    }

    /**
     * Destroy an authentication session by it's secret token.
     *
     * @param token secret token
     */
    public void destroyAuthToken(String token) {
        // Extract userId and sessionId from token
        Pair<String, String> tokenPair = decodeToken(token);
        if (tokenPair == null) {
            return;
        }
        String userId = tokenPair.getFirst();
        String sessionId = tokenPair.getSecond();

        // Delete the session from redis
        authStore.delete(userId, sessionId);
    }

    private String encodeToken(long userId, String sessionId) {
        return userId + "." + sessionId;
    }

    private Pair<String, String> decodeToken(String token) {
        // Fast-fail if the token is too long
        if (token.length() > AUTH_TOKEN_MAX_LEN) {
            return null;
        }

        // Extract userId and sessionId from the token
        String[] tokenParts = token.split("\\.", 2);
        String userId = tokenParts[0];
        String sessionId = tokenParts.length > 1 ? tokenParts[1] : "";
        if (userId.isEmpty() || sessionId.isEmpty()) {
            throw new CredentialsExpiredException("Invalid or expired auth token");
        }

        return Pair.of(userId, sessionId);
    }

    /**
     * A serializable AuthGuard implementation.
     * Using lazy-loading to access repository models.
     */
    @NoArgsConstructor
    @Data
    @ToString(of = {"userId"})
    private class AuthGuard implements com.paymybuddy.auth.AuthGuard {
        /*
         * Persistent fields
         */
        private long userId;
        private ZonedDateTime loginDate;

        /*
         * Runtime accessors/caches (must be marked transient to be excluded from the serialization).
         */
        private transient User user;

        public AuthGuard(User user) {
            this.userId = user.getId();
            this.user = user;
            this.loginDate = ZonedDateTime.now(ZoneOffset.UTC).withNano(0);
        }

        public AuthGuard(AuthGuardData data) {
            this.userId = data.getUserId();
            this.loginDate = data.getLoginDate();
        }

        public AuthGuardData toData() {
            AuthGuardData data = new AuthGuardData();
            data.setUserId(userId);
            data.setLoginDate(loginDate);
            return data;
        }

        @Override
        public boolean isAuthenticated() {
            return userId != 0L;
        }

        public User getUser() {
            if (user == null && isAuthenticated()) {
                user = AuthService.this.userProvider.getUserById(userId);
                if (user == null) {
                    throw new RuntimeException("user not found (id: " + userId + ")");
                }
            }
            return user;
        }
    }

    @NoArgsConstructor
    @Data
    public static final class AuthGuardData {
        private long userId;
        private ZonedDateTime loginDate;
    }
}
