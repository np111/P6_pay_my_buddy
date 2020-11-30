package com.paymybuddy.server.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {
    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        Jackson2ObjectMapperBuilder b = new Jackson2ObjectMapperBuilder();

        // Always serialize dates in UTC
        b.timeZone(TimeZone.getTimeZone("UTC"));
        b.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Never serialize null or transient values
        b.serializationInclusion(JsonInclude.Include.NON_NULL);
        b.featuresToEnable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);

        return b;
    }
}
