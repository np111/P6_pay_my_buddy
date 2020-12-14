package com.paymybuddy.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.server.properties.SecurityProperties;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    private final SecurityProperties securityProps;
    private final ObjectMapper objectMapper;

    @Override
    public void addCorsMappings(CorsRegistry cors) {
        cors.addMapping("/**")
                .allowedOrigins(securityProps.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods("*")
                .maxAge(TimeUnit.DAYS.toSeconds(1));
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        converters.add(converter);
    }
}
