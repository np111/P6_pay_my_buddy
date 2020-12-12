package com.paymybuddy.auth;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.auth.provider.UserProvider;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class AuthService implements InitializingBean {
    private static final String AUTH_SESS_BY_ID_HASH_KEY = "authsess.";
    private static final int AUTH_TOKEN_MAX_LEN = Long.toString(Long.MAX_VALUE).length() + 1 + new UUID(0, 0).toString().length();

    private final @Getter UserProvider userProvider;

    private final PasswordEncoder passwordEncoder;
    private String userNotFoundEncodedPassword;

    private final LettuceConnectionFactory lettuceConFactory;
    private RedisTemplate<String, ?> authSessTemplate;
    private HashOperations<String, String, AuthGuard> authSessHashOps;

    @Override
    public void afterPropertiesSet() throws Exception {
        userNotFoundEncodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        authSessTemplate = new RedisTemplate<>();
        authSessTemplate.setConnectionFactory(lettuceConFactory);
        authSessTemplate.setKeySerializer(new StringRedisSerializer());
        authSessTemplate.setHashKeySerializer(new StringRedisSerializer());
        authSessTemplate.setHashValueSerializer(new AuthSerializer());
        authSessTemplate.afterPropertiesSet();
        authSessHashOps = authSessTemplate.opsForHash();
    }

    public AuthToken login(String email, String password) {
        User user = userProvider.getUserByEmail(email);
        if (user == null) {
            // mitigate timing attack
            passwordEncoder.matches(password, userNotFoundEncodedPassword);
            throw new BadCredentialsException("Bad credentials");
        }
        if (!passwordEncoder.matches(password, user.getEncodedPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }
        if (passwordEncoder.upgradeEncoding(user.getEncodedPassword())) {
            // upgrade password encoding
            userProvider.updateEncodedPassword(user, passwordEncoder.encode(password));
        }
        return createAuthToken(user);
    }

    private AuthToken createAuthToken(User user) {
        AuthGuard auth = new AuthGuard(user);
        auth.authService = this;
        String sessionId = UUID.randomUUID().toString();
        String token = user.getId() + "." + sessionId;
        String redisKey = AUTH_SESS_BY_ID_HASH_KEY + user.getId();
        authSessHashOps.put(redisKey, sessionId, auth);
        authSessTemplate.expire(redisKey, 30, TimeUnit.DAYS);
        return AuthToken.authenticated(auth, token);
    }

    @Nullable
    public AuthToken authenticateAuthToken(AuthToken token) {
        if (token.getCredentials().length() <= AUTH_TOKEN_MAX_LEN) {
            String[] tokenParts = token.getCredentials().split("\\.", 2);
            if (tokenParts.length == 2) {
                String userId = tokenParts[0];
                String sessionId = tokenParts[1];
                AuthGuard auth = authSessHashOps.get(AUTH_SESS_BY_ID_HASH_KEY + userId, sessionId);
                if (auth != null) {
                    return AuthToken.authenticated(auth, token.getCredentials());
                }
            }
        }
        throw new CredentialsExpiredException("Invalid or expired auth token");
    }

    public void destroyAuthToken(AuthToken token) {
        if (token.getCredentials().length() <= AUTH_TOKEN_MAX_LEN) {
            String[] tokenParts = token.getCredentials().split("\\.", 2);
            if (tokenParts.length == 2) {
                String userId = tokenParts[0];
                String sessionId = tokenParts[1];
                authSessHashOps.delete(AUTH_SESS_BY_ID_HASH_KEY + userId, sessionId);
            }
        }
    }

    @NoArgsConstructor
    @Data
    @ToString(of = {"userId"})
    static class AuthGuard implements com.paymybuddy.auth.AuthGuard {
        private long userId;
        private ZonedDateTime loginDate;

        private transient AuthService authService;
        private transient User user;

        public AuthGuard(User user) {
            this.userId = user.getId();
            this.user = user;
            this.loginDate = ZonedDateTime.now(ZoneOffset.UTC).withNano(0);
        }

        @Override
        public boolean isAuthenticated() {
            return userId != 0L;
        }

        public User getUser() {
            if (user == null && isAuthenticated()) {
                user = authService.userProvider.getUserById(userId);
                if (user == null) {
                    throw new RuntimeException("user not found (id: " + userId + ")");
                }
            }
            return user;
        }
    }

    class AuthSerializer extends Jackson2JsonRedisSerializer<AuthGuard> {
        public AuthSerializer() {
            super(AuthGuard.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
            mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
            mapper.registerModule(new JavaTimeModule());
            setObjectMapper(mapper);
        }

        @Override
        public AuthGuard deserialize(byte[] data) throws SerializationException {
            AuthGuard auth = super.deserialize(data);
            if (auth != null) {
                auth.authService = AuthService.this;
            }
            return auth;
        }
    }
}
