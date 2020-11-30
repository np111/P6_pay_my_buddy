package com.paymybuddy.server.http.auth;

import com.paymybuddy.api.model.User;
import com.paymybuddy.server.service.AuthService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString(of = {"userId"})
public class Auth {
    private long userId;

    private transient @Getter(AccessLevel.NONE) AuthService authService;
    private transient User user;

    public Auth(long userId) {
        this.userId = userId;
    }

    public Auth(User user) {
        this(user.getId());
        this.user = user;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public User getUser() {
        if (user == null) {
            return user = authService.getUserService().getUserById(userId);
        }
        return user;
    }
}
