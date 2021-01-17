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

/**
 * Users management service.
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class UserService implements UserProvider {
    /**
     * Pattern that a user name must match (checked at registration/name updates).
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("^(?!.*[^ \\p{L}]{2,})(?>(?>^| )\\p{L}(?>[\\p{L}'\\-]*\\p{L})?)+$");

    /**
     * Lengths that a password must match (checked at registration/password updates).
     */
    private static final int PASSWORD_MIN_LEN = 8;
    private static final int PASSWORD_MAX_LEN = 50;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserBalanceMapper userBalanceMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Find a user by his ID.
     *
     * @param userId ID of the user to returns
     * @return the user; or {@code null} if it does not exist
     */
    @Override
    @Transactional(readOnly = true)
    @Nullable
    public User getUserById(long userId) {
        return userMapper.toUser(userRepository.findById(userId).orElse(null));
    }

    /**
     * Find a user by his email. The email is {@linkplain #normalizeEmail(String) normalized} before searching.
     *
     * @param email email of the user to returns
     * @return the user; or {@code null} if it does not exist
     */
    @Override
    @Transactional(readOnly = true)
    @Nullable
    public User getUserByEmail(String email) {
        return userMapper.toUser(getUserEntityByEmail(email));
    }

    /**
     * Find a user by his email. The email is {@linkplain #normalizeEmail(String) normalized} before searching.
     *
     * @param email email of the user to returns
     * @return the unmapped user entity; or {@code null} if it does not exist
     * @see #getUserByEmail(String)
     */
    @Transactional(readOnly = true)
    @Nullable
    public UserEntity getUserEntityByEmail(String email) {
        email = normalizeEmail(email);
        return email == null ? null : userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Find a user by his name. The name is {@linkplain #normalizeName(String) normalized} before searching.
     *
     * @param name name of the user to returns
     * @return the user; or {@code null} if it does not exist
     */
    @Transactional(readOnly = true)
    @Nullable
    public User getUserByName(String name) {
        return userMapper.toUser(getUserEntityByName(name));
    }

    /**
     * Find a user by his name. The name is {@linkplain #normalizeName(String) normalized} before searching.
     *
     * @param name name of the user to returns
     * @return the unmapped user entity; or {@code null} if it does not exist
     * @see #getUserByName(String)
     */
    @Transactional(readOnly = true)
    @Nullable
    public UserEntity getUserEntityByName(String name) {
        name = normalizeName(name);
        return name == null ? null : userRepository.findByName(name).orElse(null);
    }

    /**
     * Register a new user.
     *
     * @param name            name (which will be {@linkplain #normalizeName(String) normalized} and validated)
     * @param email           email (which will be {@linkplain #normalizeEmail(String) normalized} and validated)
     * @param password        password (which will be validated)
     * @param defaultCurrency preferred currency
     * @return the created user
     * @throws IllegalNameException            if the name is invalid
     * @throws IllegalEmailException           if the email is invalid
     * @throws TooShortPasswordException       if the password is too short
     * @throws TooLongPasswordException        if the password is too long
     * @throws EmailAlreadyRegisteredException if a user is already registered with this email
     */
    @Transactional
    public User register(String name, String email, String password, Currency defaultCurrency) {
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
            // email has an unique index, it failed
            throw new EmailAlreadyRegisteredException();
        }
        return userMapper.toUser(userEntity);
    }

    /**
     * Update the password of a user (from it's encoded version - so no validation is done).
     *
     * @param user            the user
     * @param encodedPassword the new encoded password
     */
    @Override
    @Transactional
    public void updateEncodedPassword(User user, String encodedPassword) {
        userRepository.updatePassword(user.getId(), encodedPassword);
        user.setEncodedPassword(encodedPassword);
    }

    /**
     * Returns the balances of a user.
     * <p>
     * The default currency balance is always included (first). Others are only included when they are non-zero.
     *
     * @param userId ID of the user to returns balances
     * @return the user balances; or {@code null} if the user does not exists
     */
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

    /**
     * Normalize a name.
     * <p>
     * Calling this method with an already normalized name will always returns the same result!
     *
     * @param name name to normalize
     * @return the normalized name; or {@code null} if it can't be
     */
    @Nullable
    public String normalizeName(String name) {
        // normalize
        name = StringUtils.normalizeSpace(name);

        // length check
        if (name.isEmpty() || name.length() > IsName.NAME_MAX_LEN) {
            return null;
        }
        return name;
    }

    String validateAndNormalizeNewName(String name) {
        name = normalizeName(name);
        if (name == null || !NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalNameException();
        }
        return name;
    }

    /**
     * Normalize an email.
     * <p>
     * Calling this method with an already normalized email will always returns the same result!
     *
     * @param email email to normalize
     * @return the normalized email; or {@code null} if it can't be
     */
    @Nullable
    public String normalizeEmail(String email) {
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

    String validateAndNormalizeNewEmail(String email) {
        email = normalizeEmail(email);
        if (email == null || !GenericValidator.isEmail(email)) {
            throw new IllegalEmailException();
        }
        return email;
    }

    void validateNewPassword(String password) {
        if (password.length() < PASSWORD_MIN_LEN) {
            throw new TooShortPasswordException(PASSWORD_MIN_LEN);
        }
        if (password.length() > PASSWORD_MAX_LEN) {
            throw new TooLongPasswordException(PASSWORD_MAX_LEN);
        }
    }
}
