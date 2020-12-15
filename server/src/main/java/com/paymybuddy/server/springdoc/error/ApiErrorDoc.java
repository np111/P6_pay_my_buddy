package com.paymybuddy.server.springdoc.error;

import com.paymybuddy.api.model.ApiError;
import com.paymybuddy.api.model.ApiError.ErrorType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Add service errors documentation from the {@link ApiErrorResponse} annotation.
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Scope("singleton")
public class ApiErrorDoc {
    private final BeanFactory beanFactory;

    public void customize(Operation operation, HandlerMethod handlerMethod) {
        ApiErrorResponse[] responses = handlerMethod.getMethod().getAnnotationsByType(ApiErrorResponse.class);
        for (ApiErrorResponse response : responses) {
            addApiErrorResponse(operation, handlerMethod, response);
        }
    }

    private void addApiErrorResponse(Operation operation, HandlerMethod handlerMethod, ApiErrorResponse response) {
        ApiError apiError;
        if (!response.method().isEmpty()) {
            apiError = callApiErrorMethod(handlerMethod, response.method());
        } else {
            apiError = new ApiError();
        }
        if (response.type() != ErrorType.UNKNOWN) {
            apiError.setType(response.type());
        }
        if (response.status() != 0) {
            apiError.setStatus(response.status());
        }
        if (!response.code().isEmpty()) {
            apiError.setCode(ApiError.ErrorCode.valueOf(response.code()));
        }
        if (!response.message().isEmpty()) {
            apiError.setMessage(response.message());
        }
        addApiErrorResponse(operation, apiError, response.description(), response.condition());
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

    @SneakyThrows
    private ApiError callApiErrorMethod(HandlerMethod handlerMethod, String methodName) {
        Method method = findMethod(handlerMethod, methodName);
        if (method == null) {
            throw new IllegalArgumentException("Method not found: " + handlerMethod + " -> " + methodName);
        }
        method.setAccessible(true);

        List<Object> args = new ArrayList<>();
        for (Class<?> parameterType : method.getParameterTypes()) {
            args.add(parameterType.newInstance());
        }

        Object instance = Modifier.isStatic(method.getModifiers()) ? null : beanFactory.getBean(method.getDeclaringClass());

        Object ret = method.invoke(instance, args.toArray());
        return ret instanceof ResponseEntity ? (ApiError) ((ResponseEntity<?>) ret).getBody() : (ApiError) ret;
    }

    private Method findMethod(HandlerMethod handlerMethod, String methodName) {
        Class<?> clazz = handlerMethod.getMethod().getDeclaringClass();
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> ApiError.class.isAssignableFrom(m.getReturnType())
                        || ResponseEntity.class.isAssignableFrom(m.getReturnType()))
                .min(Comparator.comparingInt(Method::getParameterCount)).orElse(null);
    }
}
