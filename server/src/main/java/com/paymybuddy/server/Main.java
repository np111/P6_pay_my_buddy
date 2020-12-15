package com.paymybuddy.server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static com.paymybuddy.server.Main.NAME;
import static com.paymybuddy.server.Main.VERSION;

@SpringBootApplication(scanBasePackages = "com.paymybuddy")
@ConfigurationPropertiesScan(basePackages = "com.paymybuddy.server.properties")
@EnableJpaRepositories("com.paymybuddy.persistence.repository")
@EntityScan("com.paymybuddy.persistence.entity")
@OpenAPIDefinition(
        info = @Info(
                title = NAME,
                version = VERSION
        )
)
public class Main {
    public static final String NAME = "PayMyBuddy API";
    public static final String VERSION = "1.0";

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
