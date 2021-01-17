package com.paymybuddy.auth.mock;

import com.paymybuddy.auth.AuthStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestAuthConfig {
    @Bean(name = "new-password-encoder")
    @Primary
    public PasswordEncoder getNewPasswordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2B, 5);
    }

    @Bean(name = "old-password-encoder")
    public PasswordEncoder getOldPasswordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2B, 4);
    }

    @Bean
    public AuthStore getAuthStore() {
        return new MockAuthStore();
    }
}
