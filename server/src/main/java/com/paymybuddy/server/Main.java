package com.paymybuddy.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.paymybuddy")
@ConfigurationPropertiesScan(basePackages = "com.paymybuddy.server.properties")
@EnableJpaRepositories("com.paymybuddy.persistence.repository")
@EntityScan("com.paymybuddy.persistence.entity")
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
