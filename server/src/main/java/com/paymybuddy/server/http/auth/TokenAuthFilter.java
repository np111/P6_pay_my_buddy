package com.paymybuddy.server.http.auth;

import com.paymybuddy.server.http.controller.ExceptionController;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Scope("singleton")
@Order(SecurityProperties.DEFAULT_FILTER_ORDER + 1) // just after spring-security filters
public class TokenAuthFilter extends OncePerRequestFilter {
    private final AuthenticationManager authManager;
    private final ExceptionController exceptionController;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String token = req.getHeader("x-auth-token");
        if (token != null && !token.isEmpty()) {
            try {
                Authentication authResult = authManager.authenticate(AuthToken.unauthenticated(token));
                SecurityContextHolder.getContext().setAuthentication(authResult);
            } catch (AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                exceptionController.handle(req, res, ex);
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
