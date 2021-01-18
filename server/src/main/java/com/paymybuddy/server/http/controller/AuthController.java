package com.paymybuddy.server.http.controller;

import com.paymybuddy.api.model.auth.LoginResponse;
import com.paymybuddy.api.request.auth.LoginRequest;
import com.paymybuddy.auth.AuthGuard;
import com.paymybuddy.auth.AuthService;
import com.paymybuddy.auth.AuthToken;
import com.paymybuddy.server.http.util.JsonRequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth", description = "Authentication operations")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Authenticate a user using his email and password.",
            description = "A new auth-token is created and included with the response."
    )
    @PreAuthorize("isAnonymous()")
    @JsonRequestMapping(method = RequestMethod.POST, value = "/login")
    public LoginResponse login(
            @RequestBody @Validated LoginRequest body
    ) {
        AuthToken authResult = authService.login(body.getEmail(), body.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authResult);
        return buildLoginResponse(authResult.getCredentials(), authResult.getPrincipal());
    }

    @Operation(
            summary = "Returns the currently authenticated user.",
            description = "It's auth-token is not included with the response."
    )
    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/remember")
    public LoginResponse remember(
            @AuthenticationPrincipal AuthGuard auth
    ) {
        return buildLoginResponse(null, auth);
    }

    private LoginResponse buildLoginResponse(String token, AuthGuard auth) {
        return LoginResponse.builder()
                .token(token)
                .user(auth.getUser())
                .build();
    }

    @Operation(
            summary = "Destroy the auth-token of the currently authenticated user."
    )
    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.POST, value = "/logout")
    public ResponseEntity<Void> logout() {
        System.out.println("((AuthToken) SecurityContextHolder.getContext().getAuthentication()).getCredentials()="
        +((AuthToken) SecurityContextHolder.getContext().getAuthentication()).getCredentials());
        authService.destroyAuthToken(((AuthToken) SecurityContextHolder.getContext().getAuthentication()).getCredentials());
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
