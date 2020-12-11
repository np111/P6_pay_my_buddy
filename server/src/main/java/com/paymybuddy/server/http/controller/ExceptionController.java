package com.paymybuddy.server.http.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.paymybuddy.api.model.ApiError;
import com.paymybuddy.api.model.ApiError.ErrorCode;
import com.paymybuddy.api.model.ApiError.ErrorType;
import com.paymybuddy.server.http.util.ApiException;
import com.paymybuddy.business.exception.PreconditionException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ControllerAdvice
@Scope("singleton")
public class ExceptionController implements AccessDeniedHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    private final ObjectMapper objectMapper;

    /**
     * Handles ApiError.
     */
    @ExceptionHandler(ApiException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleApiException(ApiException e, HttpServletRequest req) {
        return errorToResponse(e.getError());
    }

    /**
     * Handles missing handlers ("404 errors").
     * <p>
     * Returns a CLIENT/BAD_REQUEST error.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleNoHandlerFoundException(NoHandlerFoundException e,
            HttpServletRequest req) {
        return errorToResponse(errorBadRequest(e.getMessage()));
    }

    /**
     * Handles non-readable requests (eg. invalid json).
     * <p>
     * Returns a CLIENT/BAD_REQUEST error.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
            HttpServletRequest req) {
        if (e.getMessage() != null && e.getMessage().startsWith("Required request body is missing")) {
            return errorToResponse(errorValidationFailed("is required", "body", null, null));
        }
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ex = (InvalidFormatException) e.getCause();
            String parameter = ex.getPath().stream().map(JsonMappingException.Reference::getFieldName).collect(Collectors.joining("."));
            Map<String, Object> attributes = new LinkedHashMap<>();
            attributes.put("parserMessage", ex.getOriginalMessage());
            return errorToResponse(errorValidationFailed("is badly formatted", parameter, null, attributes));
        }
        return errorToResponse(errorBadRequest(e.getMessage()));
    }

    private ApiError errorBadRequest(String message) {
        return ApiError.builder()
                .type(ErrorType.CLIENT)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.BAD_REQUEST)
                .message(message)
                .build();
    }

    /**
     * Handles missing @RequestParam.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleMissingRequestParamException(MissingServletRequestParameterException e,
            HttpServletRequest req) {
        String message = "is required";
        String parameter = e.getParameterName();
        return errorToResponse(errorValidationFailed(message, parameter, null, null));
    }

    /**
     * Handles bad types @RequestParam.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e,
            HttpServletRequest req) {
        String message = "must be a " + e.getParameter().getParameterType().getSimpleName();
        String parameter = e.getName();
        return errorToResponse(errorValidationFailed(message, parameter, null, null));
    }

    /**
     * Handles failed validation.
     * <p>
     * Returns a CLIENT/VALIDATION_FAILED error.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException e,
            HttpServletRequest req) {
        ConstraintViolation<?> fieldError = e.getConstraintViolations().stream().findFirst().get();
        String message = fieldError.getMessage();
        String parameter = StreamSupport.stream(fieldError.getPropertyPath().spliterator(), false)
                .skip(1L).map(Path.Node::toString).collect(Collectors.joining("."));
        String constraint = fieldError.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        Map<String, Object> attributes = fieldError.getConstraintDescriptor().getAttributes();
        return errorToResponse(errorValidationFailed(message, parameter, constraint, attributes));
    }

    /**
     * Handles failed validation.
     * <p>
     * Returns a CLIENT/VALIDATION_FAILED error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
            HttpServletRequest req) {
        // Only handles the first FieldError if there is one
        Optional<FieldError> fieldError = e.getBindingResult()
                .getAllErrors().stream().filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error).findFirst();
        if (fieldError.isPresent()) {
            String message = fieldError.get().getDefaultMessage();
            String parameter = fieldError.get().getField();
            String constraint = fieldError.get().getCode();
            // TODO: retrieves attributes
            return errorToResponse(errorValidationFailed(message, parameter, constraint, null));
        }

        // Or handles the first misc error
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        String message = objectError.getDefaultMessage();
        String constraint = objectError.getCode();
        // TODO: retrieves attributes
        return errorToResponse(errorValidationFailed(message, null, constraint, null));
    }

    /**
     * Handles failed validation.
     * <p>
     * Returns a CLIENT/VALIDATION_FAILED error.
     */
    @ExceptionHandler(PreconditionException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handlePreconditionException(PreconditionException e, HttpServletRequest req) {
        return errorToResponse(errorValidationFailed(e.getMessage(), e.getParameter(), e.getConstraint(), e.getAttributes()));
    }

    private ApiError errorValidationFailed(String message, String parameter, String constraint,
            Map<String, Object> attributes) {
        if (parameter != null) {
            message = parameter + " " + message;
        }
        ApiError.Builder res = ApiError.builder()
                .type(ErrorType.CLIENT)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.VALIDATION_FAILED)
                .message("Validation failed: " + message);
        if (parameter != null) {
            res.metadata("parameter", parameter);
        }
        if (constraint != null) {
            res.metadata("constraint", constraint);
        }
        if (attributes != null) {
            attributes.forEach((name, value) -> {
                switch (name) {
                    case "groups":
                    case "message":
                    case "payload":
                        break;
                    default:
                        res.metadata(name, value);
                }
            });
        }
        return res.build();
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        if (!res.isCommitted()) {
            send(res, handleAccessDeniedException(ex, req));
        }
    }

    public void handle(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        if (!res.isCommitted()) {
            send(res, handleAuthenticationException(ex, req));
        }
    }

    /**
     * Handles spring-security access errors.
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest req) {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.CLIENT)
                .status(HttpStatus.FORBIDDEN.value())
                .code(ErrorCode.ACCESS_DENIED)
                .message("Access is denied")
                .build());
    }

    /**
     * Handles spring-security auth errors.
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException ex, HttpServletRequest req) {
        if (ex instanceof BadCredentialsException) {
            return errorToResponse(ApiError.builder()
                    .type(ErrorType.SERVICE)
                    .status(HttpStatus.FORBIDDEN.value())
                    .code(ErrorCode.INVALID_CREDENTIALS)
                    .message("Authentication failed: " + ex.getMessage())
                    .build());
        } else {
            return errorToResponse(ApiError.builder()
                    .type(ErrorType.CLIENT)
                    .status(HttpStatus.FORBIDDEN.value())
                    .code(ErrorCode.ACCESS_DENIED)
                    .message("Authentication failed: " + ex.getMessage())
                    .metadata("invalidToken", ex instanceof CredentialsExpiredException)
                    .build());
        }
    }

    /**
     * Handles all others exceptions.
     * <p>
     * Returns a UNKNOWN/SERVER_EXCEPTION error.
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleOthersException(Exception e, HttpServletRequest req) {
        logger.error("Unhandled request exception:", e);
        return errorToResponse(ApiError.builder()
                .type(ErrorType.UNKNOWN)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.SERVER_EXCEPTION)
                .message("Internal server error (" + e.getClass().getSimpleName() + ")")
                .build());
    }

    private void send(HttpServletResponse res, ResponseEntity<ApiError> response) throws IOException {
        res.setHeader("Content-Type", "application/json; charset=utf-8");
        response.getHeaders().forEach((key, values) -> values.forEach(value -> res.addHeader(key, value)));
        res.setStatus(response.getStatusCodeValue());
        objectMapper.writeValue(res.getWriter(), response.getBody());
    }

    static ResponseEntity<ApiError> errorToResponse(ApiError error) {
        return new ResponseEntity<>(error, HttpStatus.valueOf(error.getStatus()));
    }
}
