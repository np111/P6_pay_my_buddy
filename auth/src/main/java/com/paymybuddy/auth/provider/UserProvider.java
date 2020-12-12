package com.paymybuddy.auth.provider;

import com.paymybuddy.api.model.user.User;
import org.springframework.lang.Nullable;

public interface UserProvider {
    @Nullable
    User getUserById(long userId);

    @Nullable
    User getUserByEmail(String email);

    void updateEncodedPassword(User user, String encodedPassword);
}
