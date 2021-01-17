package com.paymybuddy.auth.provider;

import com.paymybuddy.auth.AuthService;
import com.paymybuddy.auth.AuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * An {@link AuthenticationProvider} for {@linkplain AuthToken token strings}, using our own
 * {@linkplain AuthService authentication service}.
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
@Scope("singleton")
public class TokenAuthProvider implements AuthenticationProvider {
    private final AuthService authService;

    @Override
    public AuthToken authenticate(Authentication authentication) throws AuthenticationException {
        return authService.authenticateAuthToken(((AuthToken) authentication).getCredentials());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AuthToken.class.isAssignableFrom(authentication);
    }
}
