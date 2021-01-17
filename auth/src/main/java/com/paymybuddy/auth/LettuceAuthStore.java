package com.paymybuddy.auth;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paymybuddy.auth.AuthService.AuthGuardData;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class LettuceAuthStore implements AuthStore {
    /**
     * Prefix of the redis hash-keys used to store authentication sessions.
     */
    private static final String AUTH_SESS_BY_ID_HASH_KEY = "authsess.";

    private final RedisTemplate<String, ?> authSessTemplate;
    private final HashOperations<String, String, AuthGuardData> authSessHashOps;

    public LettuceAuthStore(@Autowired LettuceConnectionFactory lettuceConFactory) {
        authSessTemplate = new RedisTemplate<>();
        authSessTemplate.setConnectionFactory(lettuceConFactory);
        authSessTemplate.setKeySerializer(new StringRedisSerializer());
        authSessTemplate.setHashKeySerializer(new StringRedisSerializer());
        authSessTemplate.setHashValueSerializer(new AuthSerializer());
        authSessTemplate.afterPropertiesSet();
        authSessHashOps = authSessTemplate.opsForHash();
    }

    @Override
    public void save(AuthGuardData data, String sessionId, long timeValue, TimeUnit timeUnit) {
        String redisKey = AUTH_SESS_BY_ID_HASH_KEY + data.getUserId();
        authSessHashOps.put(redisKey, sessionId, data);
        authSessTemplate.expire(redisKey, timeValue, timeUnit);
    }

    @Override
    public AuthGuardData load(String userId, String sessionId) {
        // TODO: Store expireDate with AuthGuardData and check-it here?
        return authSessHashOps.get(AUTH_SESS_BY_ID_HASH_KEY + userId, sessionId);
    }

    @Override
    public void delete(String userId, String sessionId) {
        authSessHashOps.delete(AUTH_SESS_BY_ID_HASH_KEY + userId, sessionId);
    }

    /**
     * Jackson serializer for AuthService.AuthGuard implementation.
     */
    private static class AuthSerializer extends Jackson2JsonRedisSerializer<AuthGuardData> {
        public AuthSerializer() {
            super(AuthGuardData.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
            mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
            mapper.registerModule(new JavaTimeModule());
            setObjectMapper(mapper);
        }
    }
}
