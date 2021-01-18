package com.paymybuddy.server.mock;

import com.paymybuddy.server.config.JacksonConfig;
import com.paymybuddy.server.config.MethodSecurityConfig;
import com.paymybuddy.server.config.WebMvcConfig;
import com.paymybuddy.server.config.WebSecurityConfig;
import com.paymybuddy.server.properties.SecurityProperties;
import java.util.Collections;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({JacksonConfig.class, MethodSecurityConfig.class, WebMvcConfig.class, WebSecurityConfig.class})
public class TestControllerConfig {
    @Bean
    public SecurityProperties getSecurityProperties() {
        SecurityProperties props = new SecurityProperties();
        props.setAllowedOrigins(Collections.emptyList());
        return props;
    }
}
