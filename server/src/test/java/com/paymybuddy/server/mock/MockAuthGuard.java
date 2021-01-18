package com.paymybuddy.server.mock;

import com.paymybuddy.api.model.user.User;
import com.paymybuddy.auth.AuthGuard;
import com.paymybuddy.auth.AuthToken;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@UtilityClass
public class MockAuthGuard {
    public static AuthGuard get() {
        return ((AuthToken) SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }

    public static AuthGuard create() {
        User user = MockUsers.newUser(1L);
        return new AuthGuard() {
            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public long getUserId() {
                return user.getId();
            }

            @Override
            public User getUser() {
                return user;
            }
        };
    }

    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithAuthTokenSecurityContextFactory.class)
    public @interface WithAuthToken {
    }

    private static class WithAuthTokenSecurityContextFactory implements WithSecurityContextFactory<WithAuthToken> {
        @Override
        public SecurityContext createSecurityContext(WithAuthToken annotation) {
            SecurityContext ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(AuthToken.authenticated(create(), "TOKEN"));
            return ctx;
        }
    }
}
