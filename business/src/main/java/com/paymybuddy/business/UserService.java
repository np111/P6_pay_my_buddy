package com.paymybuddy.business;

import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.api.model.user.UserBalance;
import com.paymybuddy.api.model.user.UserBalancesResponse;
import com.paymybuddy.api.util.validation.constraint.IsEmail;
import com.paymybuddy.api.util.validation.constraint.IsName;
import com.paymybuddy.auth.provider.UserProvider;
import com.paymybuddy.business.exception.EmailAlreadyRegisteredException;
import com.paymybuddy.business.exception.IllegalEmailException;
import com.paymybuddy.business.exception.IllegalNameException;
import com.paymybuddy.business.exception.TooLongPasswordException;
import com.paymybuddy.business.exception.TooShortPasswordException;
import com.paymybuddy.business.mapper.UserBalanceMapper;
import com.paymybuddy.business.mapper.UserMapper;
import com.paymybuddy.persistence.entity.UserEntity;
import com.paymybuddy.persistence.repository.UserRepository;
import java.math.BigDecimal;
import java.net.IDN;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class UserService implements UserProvider {
    private static final Pattern NAME_PATTERN = Pattern.compile("^(?>(?>^| )\\p{L}(?>[\\p{L}'\\-]*\\p{L})?)+$");
    private static final int PASSWORD_MIN_LEN = 8;
    private static final int PASSWORD_MAX_LEN = 50;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserBalanceMapper userBalanceMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    @Nullable
    public User getUserById(long userId) {
        return userMapper.toUser(userRepository.findById(userId).orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    @Nullable
    public User getUserByEmail(String email) {
        return userMapper.toUser(getUserEntityByEmail(email));
    }

    @Transactional(readOnly = true)
    @Nullable
    public UserEntity getUserEntityByEmail(String email) {
        email = normalizeEmail(email);
        return email == null ? null : userRepository.findByEmail(email).orElse(null);
    }

    @Transactional(readOnly = true)
    @Nullable
    public User getUserByName(String name) {
        return userMapper.toUser(getUserEntityByName(name));
    }

    @Transactional(readOnly = true)
    @Nullable
    public UserEntity getUserEntityByName(String name) {
        name = normalizeName(name);
        return name == null ? null : userRepository.findByName(name).orElse(null);
    }

    @Transactional
    public User register(String name, String email, String password, Currency defaultCurrency) throws IllegalNameException, IllegalEmailException, EmailAlreadyRegisteredException {
        name = validateAndNormalizeNewName(name);
        email = validateAndNormalizeNewEmail(email);
        validateNewPassword(password);

        UserEntity userEntity = new UserEntity();
        userEntity.setName(name);
        userEntity.setEmail(email);
        userEntity.setEncodedPassword(passwordEncoder.encode(password));
        userEntity.setDefaultCurrency(defaultCurrency);
        try {
            userRepository.save(userEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyRegisteredException();
        }
        return userMapper.toUser(userEntity);
    }

    @Override
    @Transactional
    public void updateEncodedPassword(User user, String encodedPassword) {
        userRepository.updatePassword(user.getId(), encodedPassword);
        user.setEncodedPassword(encodedPassword);
    }

    @Transactional(readOnly = true)
    @Nullable
    public UserBalancesResponse getUserBalances(long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if (userEntity == null) {
            return null;
        }

        Currency defaultCurrency = userEntity.getDefaultCurrency();
        List<UserBalance> balances = userEntity.getBalances().stream()
                .filter(b -> BigDecimal.ZERO.compareTo(b.getAmount()) != 0)
                .sorted((a, b) -> {
                    if (a.getCurrency() == defaultCurrency) {
                        return -1;
                    }
                    if (b.getCurrency() == defaultCurrency) {
                        return 1;
                    }
                    return a.getCurrency().ordinal() - b.getCurrency().ordinal();
                })
                .map(userBalanceMapper::toUserBalance)
                .collect(Collectors.toList());
        if (balances.stream().noneMatch(b -> b.getCurrency() == defaultCurrency)) {
            balances.add(0, UserBalance.builder()
                    .currency(userEntity.getDefaultCurrency())
                    .amount(new BigDecimal(0))
                    .build());
        }
        return UserBalancesResponse.builder()
                .defaultCurrency(defaultCurrency)
                .balances(balances)
                .build();
    }

    @Nullable
    private String normalizeName(String name) {
        // normalize
        name = StringUtils.normalizeSpace(name);

        // length check
        if (name.isEmpty() || name.length() > IsName.NAME_MAX_LEN) {
            return null;
        }
        return name;
    }

    private String validateAndNormalizeNewName(String name) {
        name = normalizeName(name);
        if (name == null || !NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalNameException();
        }
        return name;
    }

    @Nullable
    private String normalizeEmail(String email) {
        // normalize
        String[] emailParts = email.split("@", 2);
        if (emailParts.length != 2) {
            return null;
        }
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

    private String validateAndNormalizeNewEmail(String email) {
        email = normalizeEmail(email);
        if (email == null || !GenericValidator.isEmail(email)) {
            throw new IllegalEmailException();
        }
        return email;
    }

    private void validateNewPassword(String password) {
        if (password.length() < PASSWORD_MIN_LEN) {
            throw new TooShortPasswordException(PASSWORD_MIN_LEN);
        }
        if (password.length() > PASSWORD_MAX_LEN) {
            throw new TooLongPasswordException(PASSWORD_MAX_LEN);
        }
    }
}
