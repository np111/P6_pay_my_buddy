package com.paymybuddy.server.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("com.paymybuddy.persistence.repository")
@EntityScan("com.paymybuddy.persistence.entity")
public class RepositoryConfig {
}
