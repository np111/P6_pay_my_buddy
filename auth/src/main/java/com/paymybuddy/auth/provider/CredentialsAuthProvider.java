package com.paymybuddy.auth.provider;

import com.paymybuddy.auth.AuthService;
import com.paymybuddy.auth.AuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * An {@link AuthenticationProvider} for {@linkplain UsernamePasswordAuthenticationToken username/password credentials},
 * using our own {@linkplain AuthService authentication service}.
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Scope("singleton")
public class CredentialsAuthProvider implements AuthenticationProvider {
    private final AuthService authService;

    @Override
    public AuthToken authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken credentials = (UsernamePasswordAuthenticationToken) authentication;
        return authService.login(determineEmail(credentials), determinePassword(credentials));
    }

    private String determineEmail(Authentication authentication) {
        // Using "NONE_PROVIDED" like spring does. Unfortunately, they do not expose any constant.
        // See https://github.com/spring-projects/spring-security/blob/f614a8230c84a505597de0bd6380e5e2fea117ea/core/src/main/java/org/springframework/security/authentication/dao/AbstractUserDetailsAuthenticationProvider.java#L172
        return authentication.getPrincipal() == null ? "NONE_PROVIDED" : authentication.getName();
    }

    private String determinePassword(Authentication authentication) {
        return authentication.getCredentials().toString();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
