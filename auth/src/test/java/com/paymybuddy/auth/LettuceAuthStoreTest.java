package com.paymybuddy.auth;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LettuceAuthStoreTest {
    @Mock
    private LettuceConnectionFactory conFactory;
    @Spy
    private RedisConnection redisCon;
    private LettuceAuthStore authStore;

    @BeforeEach
    void setUp() {
        when(conFactory.getConnection()).thenReturn(redisCon);
        authStore = new LettuceAuthStore(conFactory);
    }

    @Test
    void save() {
        AuthService.AuthGuardData data = new AuthService.AuthGuardData();
        data.setUserId(77L);
        authStore.save(data, "b1333550-ac15-4cc1-89da-45633b2d1db5", 30, TimeUnit.DAYS);

        verify(redisCon, times(1)).hSet(
                eq("authsess.77".getBytes(StandardCharsets.UTF_8)),
                eq("b1333550-ac15-4cc1-89da-45633b2d1db5".getBytes(StandardCharsets.UTF_8)),
                any());
    }

    @Test
    void load() {
        when(redisCon.hGet(
                eq("authsess.77".getBytes(StandardCharsets.UTF_8)),
                eq("b1333550-ac15-4cc1-89da-45633b2d1db5".getBytes(StandardCharsets.UTF_8))
        )).thenReturn("{\"userId\":77}".getBytes(StandardCharsets.UTF_8));

        AuthService.AuthGuardData data = authStore.load("77", "b1333550-ac15-4cc1-89da-45633b2d1db5");
        assertEquals(77L, data.getUserId());
    }

    @Test
    void delete() {
        authStore.delete("77", "b1333550-ac15-4cc1-89da-45633b2d1db5");

        verify(redisCon, times(1)).hDel(
                eq("authsess.77".getBytes(StandardCharsets.UTF_8)),
                eq("b1333550-ac15-4cc1-89da-45633b2d1db5".getBytes(StandardCharsets.UTF_8)));
    }
}
