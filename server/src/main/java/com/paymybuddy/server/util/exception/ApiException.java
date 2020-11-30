package com.paymybuddy.server.util.exception;

import com.paymybuddy.api.model.ApiError;
import com.paymybuddy.server.http.controller.ExceptionController;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;

/**
 * Wrapper to return an ApiError from a request.
 *
 * @see ExceptionController#handleApiException(ApiException, HttpServletRequest)
 */
@Getter
public class ApiException extends FastRuntimeException {
    private final ApiError error;

    public ApiException(ApiError error) {
        this.error = error;
    }
}
