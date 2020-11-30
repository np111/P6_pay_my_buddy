package com.paymybuddy.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.User;
import com.paymybuddy.server.http.auth.Auth;
import com.paymybuddy.server.http.auth.AuthToken;
import com.paymybuddy.server.jpa.entity.UserEntity;
import com.paymybuddy.server.jpa.mapper.UserMapper;
import com.paymybuddy.server.jpa.repository.UserRepository;
import com.paymybuddy.server.util.exception.FastRuntimeException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.GenericValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
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
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class AuthService implements InitializingBean {
    private static final String AUTH_SESS_HASH_KEY = "authsess.";
    private static final int AUTH_TOKEN_MAX_LEN = Long.toString(Long.MAX_VALUE).length() + 1 + new UUID(0, 0).toString().length();

    private final @Getter PasswordEncoder passwordEncoder;
    private String userNotFoundEncodedPassword;

    private final @Getter UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final ObjectMapper objectMapper;
    private final LettuceConnectionFactory lettuceConFactory;
    private RedisTemplate<String, ?> authSessTemplate;
    private HashOperations<String, String, Auth> authSessHashOps;

    @Override
    public void afterPropertiesSet() throws Exception {
        userNotFoundEncodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        authSessTemplate = new RedisTemplate<>();
        authSessTemplate.setConnectionFactory(lettuceConFactory);
        authSessTemplate.setKeySerializer(new StringRedisSerializer());
        authSessTemplate.setHashKeySerializer(new StringRedisSerializer());
        authSessTemplate.setHashValueSerializer(new AuthSerializer(objectMapper));
        authSessTemplate.afterPropertiesSet();
        authSessHashOps = authSessTemplate.opsForHash();
    }

    @Transactional
    public AuthToken login(String email, String password) {
        User user = userService.getUserByEmail(email);
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
            userRepository.updatePassword(user.getId(), passwordEncoder.encode(password));
        }
        return createAuthToken(user);
    }

    private AuthToken createAuthToken(User user) {
        Auth auth = new Auth(user);
        auth.setAuthService(this);
        String sessionId = UUID.randomUUID().toString();
        String token = user.getId() + "." + sessionId;
        String redisKey = AUTH_SESS_HASH_KEY + user.getId();
        authSessHashOps.put(redisKey, sessionId, auth);
        authSessTemplate.expire(redisKey, 30, TimeUnit.DAYS);
        return new AuthToken(auth, token);
    }

    @Nullable
    public AuthToken authenticateAuthToken(AuthToken token) {
        if (token.getCredentials().length() <= AUTH_TOKEN_MAX_LEN) {
            String[] tokenParts = token.getCredentials().split("\\.", 2);
            if (tokenParts.length == 2) {
                String userId = tokenParts[0];
                String sessionId = tokenParts[1];
                String redisKey = AUTH_SESS_HASH_KEY + userId;
                Auth user = authSessHashOps.get(redisKey, sessionId);
                if (user != null) {
                    return new AuthToken(user, token.getCredentials());
                }
            }
        }
        throw new CredentialsExpiredException("Invalid or expired auth token");
    }

    @Transactional
    public User register(String name, String email, String password, Currency defaultCurrency) throws IllegalNameException, IllegalEmailException, EmailAlreadyRegisteredException {
        name = validateAndNormalizeNewName(name);
        email = validateAndNormalizeNewEmail(email);
        validateNewPassword(password);

        UserEntity userEntity = new UserEntity();
        userEntity.setName(name);
        userEntity.setEmail(email);
        userEntity.setEncodedPassword(passwordEncoder.encode(password));
        userEntity.setDefaultCurrency(defaultCurrency);
        try {
            userRepository.save(userEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyRegisteredException();
        }
        return userMapper.toUser(userEntity);
    }

    private String validateAndNormalizeNewName(String name) {
        name = userService.normalizeName(name);
        if (name == null) {
            throw new IllegalNameException();
        }
        // TODO: Advanced validation
        return userService.normalizeName(name);
    }

    private String validateAndNormalizeNewEmail(String email) {
        email = userService.normalizeEmail(email);
        if (email == null || !GenericValidator.isEmail(email)) {
            throw new IllegalEmailException();
        }
        return email;
    }

    private void validateNewPassword(String password) {
        // TODO(high): validate password preconditions (minimum length, symbols?, etc)
    }

    public class AuthSerializer extends Jackson2JsonRedisSerializer<Auth> {
        public AuthSerializer(ObjectMapper objectMapper) {
            super(Auth.class);
            setObjectMapper(objectMapper);
        }

        @Override
        public Auth deserialize(byte[] bytes) throws SerializationException {
            Auth auth = super.deserialize(bytes);
            if (auth != null) {
                auth.setAuthService(AuthService.this);
            }
            return auth;
        }
    }

    public static class IllegalNameException extends FastRuntimeException {
    }

    public static class IllegalEmailException extends FastRuntimeException {
    }

    public static class EmailAlreadyRegisteredException extends FastRuntimeException {
    }
}
