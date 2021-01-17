package com.paymybuddy.auth;

import com.paymybuddy.auth.AuthService.AuthGuardData;
import java.util.concurrent.TimeUnit;

public interface AuthStore {
    void save(AuthGuardData data, String sessionId, long timeValue, TimeUnit timeUnit);

    AuthGuardData load(String userId, String sessionId);

    void delete(String userId, String sessionId);
}
