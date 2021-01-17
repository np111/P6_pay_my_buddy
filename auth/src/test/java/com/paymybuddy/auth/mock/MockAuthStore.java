package com.paymybuddy.auth.mock;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.paymybuddy.auth.AuthService;
import com.paymybuddy.auth.AuthStore;
import java.util.concurrent.TimeUnit;

public class MockAuthStore implements AuthStore {
    private final Table<String, String, AuthService.AuthGuardData> map = HashBasedTable.create();

    @Override
    public void save(AuthService.AuthGuardData data, String sessionId, long timeValue, TimeUnit timeUnit) {
        map.put("" + data.getUserId(), sessionId, data);
    }

    @Override
    public AuthService.AuthGuardData load(String userId, String sessionId) {
        return map.get(userId, sessionId);
    }

    @Override
    public void delete(String userId, String sessionId) {
        map.remove(userId, sessionId);
    }
}
