package com.paymybuddy.server.springdoc;

import com.paymybuddy.api.model.ApiError;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.SpringDocAnnotationsUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Scope("singleton")
public class SpringdocCustomizer implements OpenApiCustomiser, OperationCustomizer {
    private final ValidationDoc validationDoc;
    private final ApiErrorDoc apiErrorDoc;
    private final ContentTypeDoc contentTypeDoc;

    @Override
    public void customise(OpenAPI openAPI) {
        // register ApiError schema
        SpringDocAnnotationsUtils.resolveSchemaFromType(ApiError.class, openAPI.getComponents(), null);
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        validationDoc.customize(operation, handlerMethod);
        apiErrorDoc.customize(operation, handlerMethod);
        contentTypeDoc.customize(operation, handlerMethod);
        return operation;
    }
}
