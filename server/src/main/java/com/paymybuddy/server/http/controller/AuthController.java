package com.paymybuddy.server.http.controller;

import com.paymybuddy.api.model.ApiError;
import com.paymybuddy.api.model.ApiError.ErrorCode;
import com.paymybuddy.api.model.ApiError.ErrorType;
import com.paymybuddy.api.request.LoginRequest;
import com.paymybuddy.api.request.RegisterRequest;
import com.paymybuddy.api.response.LoginResponse;
import com.paymybuddy.server.http.auth.AuthGuard;
import com.paymybuddy.server.http.auth.AuthToken;
import com.paymybuddy.server.service.AuthService;
import com.paymybuddy.server.util.spring.JsonRequestMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.paymybuddy.server.http.controller.ExceptionController.errorToResponse;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final AuthService authService;

    @PreAuthorize("isAnonymous()")
    @JsonRequestMapping(method = RequestMethod.POST, value = "/register")
    public ResponseEntity<Void> register(
            @RequestBody @Validated RegisterRequest body
    ) {
        authService.register(body.getName(), body.getEmail(), body.getPassword(), body.getDefaultCurrency());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAnonymous()")
    @JsonRequestMapping(method = RequestMethod.POST, value = "/login")
    public LoginResponse login(
            @RequestBody @Validated LoginRequest body
    ) {
        AuthToken authResult = authService.login(body.getEmail(), body.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authResult);
        return buildLoginResponse(authResult.getCredentials(), authResult.getPrincipal());
    }

    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/remember")
    public LoginResponse me(@AuthenticationPrincipal AuthGuard auth) {
        return buildLoginResponse(null, auth);
    }

    private LoginResponse buildLoginResponse(String token, AuthGuard auth) {
        return LoginResponse.builder()
                .token(token)
                .user(auth.getUser())
                .build();
    }

    @ExceptionHandler(AuthService.EmailAlreadyRegisteredException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleEmailAlreadyRegisteredException() {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.SERVICE)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.EMAIL_ALREADY_EXISTS)
                .message("Email already registered")
                .build());
    }

    // TODO: handle IllegalNameException, IllegalEmailException, EmailAlreadyRegisteredException
}
