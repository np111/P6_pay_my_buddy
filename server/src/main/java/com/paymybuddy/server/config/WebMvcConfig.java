package com.paymybuddy.server.config;

import com.paymybuddy.server.properties.SecurityProperties;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer, BeanPostProcessor {
    private final SecurityProperties securityProps;
    private final Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Override
    public void addCorsMappings(CorsRegistry cors) {
        cors.addMapping("/**")
                .allowedOrigins(securityProps.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods("*")
                .maxAge(TimeUnit.DAYS.toSeconds(1));
    }

    @Bean
    @Primary
    public WebMvcProperties webMvcProperties() {
        WebMvcProperties props = new WebMvcProperties();
        // allows us to deal with nonexistent endpoints
        props.setThrowExceptionIfNoHandlerFound(true);
        return props;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new StringHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter(jackson2ObjectMapperBuilder.build()));
    }

    @SuppressWarnings("deprecation") // no alternatives yet
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // default to application/json content-type
        configurer.defaultContentType(MediaType.APPLICATION_JSON);

        // ignore the Accept header and always use the default content-type
        configurer.ignoreAcceptHeader(true);

        // don't use path extensions for content negotiation (since this is discouraged, see https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/accept/ContentNegotiationManagerFactoryBean.html)
        configurer.favorPathExtension(false);
        configurer.ignoreUnknownPathExtensions(true);
    }

    @SuppressWarnings("deprecation") // no alternatives yet
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // don't use path extensions for content negotiation (see favorPathExtension/ignoreUnknownPathExtensions above)
        configurer.setUseSuffixPatternMatch(false);

        // don't be lenient with trailing slash usages
        configurer.setUseTrailingSlashMatch(false);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerMapping) {
            RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) bean;
            // disable URL decoding before request mapping. It allows path containing '%2F' to be correctly processed.
            requestMappingHandlerMapping.setUrlDecode(false);
            // workaround to make the previous fix work (see https://jira.springsource.org/browse/SPR-11101).
            requestMappingHandlerMapping.setAlwaysUseFullPath(true);
        }
        return bean;
    }

    @Bean
    public InternalResourceViewResolver defaultViewResolver() {
        // support redirect views (for springdoc-openapi, see https://github.com/springdoc/springdoc-openapi/issues/236)
        return new InternalResourceViewResolver();
    }
}
