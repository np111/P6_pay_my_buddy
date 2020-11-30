package com.paymybuddy.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.paymybuddy.server")
@ConfigurationPropertiesScan(basePackages = "com.paymybuddy.server.properties")
public class Main {
    public static void main(String[] args) {
        System.setProperty("logging.level.org.hibernate.engine.jdbc.spi.SqlExceptionHelper", "FATAL");
        SpringApplication.run(Main.class, args);
    }
}
