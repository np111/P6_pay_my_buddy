package com.paymybuddy.server.http.filter;

import com.paymybuddy.auth.AuthToken;
import com.paymybuddy.auth.provider.TokenAuthProvider;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A filter to authenticate requests using the {@link AuthToken} passed in the "X-Auth-Token" header value.
 *
 * @see AuthToken
 * @see TokenAuthProvider
 */
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
        try {
            if (token == null) {
                if (!isAuthenticated() && !isBrowserEndpoint(req)) {
                    throw new CredentialsExpiredException("Missing auth token");
                }
            } else if (!"anonymous".equals(token)) {
                Authentication authResult = authManager.authenticate(AuthToken.unauthenticated(token));
                SecurityContextHolder.getContext().setAuthentication(authResult);
            }
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            exceptionController.handle(req, res, ex);
            return;
        }
        chain.doFilter(req, res);
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean isBrowserEndpoint(HttpServletRequest req) {
        String path = req.getServletPath();
        switch (req.getServletPath()) {
            case "/docs.html":
            case "/api-docs":
            case "/api-docs/swagger-config":
                return true;
            default:
                return path.startsWith("/swagger-ui/");
        }
    }
}
