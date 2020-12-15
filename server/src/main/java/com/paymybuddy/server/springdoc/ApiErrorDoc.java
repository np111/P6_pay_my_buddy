package com.paymybuddy.server.springdoc;

import com.paymybuddy.api.model.ApiError;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Add service errors documentation from the {@link ApiErrorResponse} annotation.
 */
@Component
@Scope("singleton")
public class ApiErrorDoc {
    public void customize(Operation operation, HandlerMethod handlerMethod) {
        // TODO
    }

    public void addApiErrorResponse(Operation operation, ApiError apiError, String description, String condition) {
        String name = "" + apiError.getStatus();
        while (operation.getResponses().containsKey(name)) {
            name += "'";
        }

        if (description == null || description.isEmpty()) {
            description = StringUtils.capitalize(StringUtils.defaultString(apiError.getMessage()));
        }

        description = "`" + apiError.getType() + "`/`" + apiError.getCode() + "` - " + description;
        if (condition != null && !condition.isEmpty()) {
            description = "*if (" + condition + "):*\n>" + description.replace("\n", "\n>");
        }

        operation.getResponses().addApiResponse(name, new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType("*/*", new MediaType()
                                .schema(new Schema<>().$ref("ApiError"))
                                .example(apiError))));
    }
}
