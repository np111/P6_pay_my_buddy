package com.paymybuddy.server.springdoc;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Replace the default "* /*" content type by "application/json".
 */
@Component
@Scope("singleton")
public class ContentTypeDoc {
    public void customize(Operation operation, HandlerMethod handlerMethod) {
        operation.getResponses().values().forEach(response -> {
            Content content = response.getContent();
            if (content != null) {
                MediaType mediaType = content.remove("*/*");
                if (mediaType != null) {
                    content.addMediaType("application/json", mediaType);
                }
            }
        });
    }
}
