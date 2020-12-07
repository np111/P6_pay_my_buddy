package com.paymybuddy.server.service;

import com.paymybuddy.api.model.user.User;
import com.paymybuddy.api.model.user.UserBalance;
import com.paymybuddy.api.util.validation.constraint.IsEmail;
import com.paymybuddy.server.jpa.entity.UserEntity;
import com.paymybuddy.server.jpa.mapper.UserBalanceMapper;
import com.paymybuddy.server.jpa.mapper.UserMapper;
import com.paymybuddy.server.jpa.repository.UserRepository;
import java.math.BigDecimal;
import java.net.IDN;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
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
    private final UserBalanceMapper userBalanceMapper;

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

    @Transactional(readOnly = true)
    @Nullable
    public List<UserBalance> getUserBalances(long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if (userEntity == null) {
            return Collections.emptyList();
        }

        List<UserBalance> ret = userEntity.getBalances().stream()
                .map(userBalanceMapper::toUserBalance)
                .collect(Collectors.toList());
        if (ret.stream().noneMatch(b -> b.getCurrency() == userEntity.getDefaultCurrency())) {
            ret.add(0, UserBalance.builder()
                    .currency(userEntity.getDefaultCurrency())
                    .amount(new BigDecimal(0))
                    .build());
        }
        return ret;
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
