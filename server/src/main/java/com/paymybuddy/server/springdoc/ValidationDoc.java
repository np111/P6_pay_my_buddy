package com.paymybuddy.server.springdoc;

import com.paymybuddy.api.model.ApiError;
import com.paymybuddy.api.model.ApiError.ErrorCode;
import io.swagger.v3.oas.models.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.method.HandlerMethod;

/**
 * Automatically document the validation (JSR 380 constraints) errors.
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Scope("singleton")
public class ValidationDoc {
    private final ApiErrorDoc apiErrorDoc;

    public void customize(Operation operation, HandlerMethod handlerMethod) {
        if (handlerMethod.getMethod().getDeclaringClass().isAnnotationPresent(Validated.class)) {
            List<ConstraintsDescriptor.Description> constraints = ConstraintsDescriptor.describeParameters(handlerMethod.getMethod());
            if (!constraints.isEmpty()) {
                ApiError apiError = ApiError.builder()
                        .type(ApiError.ErrorType.CLIENT)
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code(ErrorCode.VALIDATION_FAILED)
                        .message("Validation failed")
                        .build();
                StringBuilder description = new StringBuilder();
                description.append("A request parameter validation failed:");
                constraintsToString(description, "", constraints);
                apiErrorDoc.addApiErrorResponse(operation, apiError, description.toString(), null);
            }
        }
    }

    private void constraintsToString(StringBuilder sb, String prefix, List<ConstraintsDescriptor.Description> descriptions) {
        for (ConstraintsDescriptor.Description description : descriptions) {
            sb.append("\n").append(prefix).append("- *").append(description.getName()).append("*:");
            int i = 0;
            for (String constraint : description.getConstraints()) {
                sb.append(++i == 1 ? " " : ", ");
                sb.append(constraint);
            }
            constraintsToString(sb, prefix + "  ", description.getFields());
        }
    }
}
