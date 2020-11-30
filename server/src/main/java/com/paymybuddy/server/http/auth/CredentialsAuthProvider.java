package com.paymybuddy.server.http.auth;

import com.paymybuddy.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Scope("singleton")
public class CredentialsAuthProvider implements AuthenticationProvider {
    private final AuthService authService;

    @Override
    public AuthToken authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken credentials = (UsernamePasswordAuthenticationToken) authentication;
        return authService.login((String) credentials.getPrincipal(), (String) credentials.getCredentials());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
