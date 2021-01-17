package com.paymybuddy.server.config;

import com.paymybuddy.auth.LettuceAuthStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Configuration
public class LettuceConfig {
    private final LettuceConnectionFactory lettuceConFactory;

    @Bean
    public LettuceAuthStore getLettuceAuthStore() {
        return new LettuceAuthStore(lettuceConFactory);
    }
}
