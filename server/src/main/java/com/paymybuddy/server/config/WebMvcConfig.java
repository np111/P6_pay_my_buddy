package com.paymybuddy.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
    private final ObjectMapper objectMapper;

    @Override
    public void addCorsMappings(CorsRegistry cors) {
        cors.addMapping("/**").allowedOrigins("http://localhost:3000"); // TODO: add origins properties
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        converters.add(converter);
    }
}
