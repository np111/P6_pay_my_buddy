package com.paymybuddy.server.service;

import com.paymybuddy.api.model.User;
import com.paymybuddy.api.validation.constraint.IsEmail;
import com.paymybuddy.server.jpa.mapper.UserMapper;
import com.paymybuddy.server.jpa.repository.UserRepository;
import java.net.IDN;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Nullable
    public User getUserById(long userId) {
        return userMapper.toUser(userRepository.findById(userId).orElse(null));
    }

    @Transactional(readOnly = true)
    @Nullable
    public User getUserByEmail(String email) {
        email = normalizeEmail(email);
        return email == null ? null : userMapper.toUser(userRepository.findByEmail(email).orElse(null));
    }

    @Nullable
    public String normalizeName(String name) {
        // TODO
        return name;
    }

    @Nullable
    public String normalizeEmail(String email) {
        // normalize
        String[] emailParts = email.split("@", 2);
        String user = emailParts[0];
        String domain = emailParts[1];
        try {
            domain = IDN.toASCII(domain).toLowerCase(Locale.ROOT).replaceAll("\\.{2,}", ".");
        } catch (Exception e) {
            return null;
        }
        email = user + "@" + domain;

        // length check
        if (email.length() > IsEmail.EMAIL_MAX_LEN) {
            return null;
        }
        return email;
    }
}
