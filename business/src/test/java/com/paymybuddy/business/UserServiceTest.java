package com.paymybuddy.business;

import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.api.model.user.UserBalancesResponse;
import com.paymybuddy.business.exception.EmailAlreadyRegisteredException;
import com.paymybuddy.business.exception.IllegalEmailException;
import com.paymybuddy.business.exception.IllegalNameException;
import com.paymybuddy.business.exception.TooLongPasswordException;
import com.paymybuddy.business.exception.TooShortPasswordException;
import com.paymybuddy.business.mapper.UserBalanceMapper;
import com.paymybuddy.business.mapper.UserBalanceMapperImpl;
import com.paymybuddy.business.mapper.UserMapper;
import com.paymybuddy.business.mapper.UserMapperImpl;
import com.paymybuddy.business.mock.MockUsers;
import com.paymybuddy.business.mock.TestBusinessConfig;
import com.paymybuddy.persistence.entity.UserEntity;
import com.paymybuddy.persistence.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {UserService.class, UserMapperImpl.class, UserBalanceMapperImpl.class})
@Import(TestBusinessConfig.class)
class UserServiceTest {
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBalanceMapper userBalanceMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void getUserById() {
        UserEntity user = MockUsers.newUserEntity(1L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertEquals(userMapper.toUser(user), userService.getUserById(user.getId()));
        assertNull(userService.getUserById(2L));
    }

    @Test
    void getUserByEmail() {
        UserEntity user = MockUsers.newUserEntity(1L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertEquals(userMapper.toUser(user), userService.getUserByEmail(user.getEmail()));
        assertNull(userService.getUserByEmail("2@domain.tld"));

        assertEquals(user, userService.getUserEntityByEmail(user.getEmail()));
        assertNull(userService.getUserEntityByEmail("2@domain.tld"));
        assertNull(userService.getUserEntityByEmail("xxx"));
    }

    @Test
    void getUserByName() {
        UserEntity user = MockUsers.newUserEntity(1L);
        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        assertEquals(userMapper.toUser(user), userService.getUserByName(user.getName()));
        assertNull(userService.getUserByName("#2"));

        assertEquals(user, userService.getUserEntityByName(user.getName()));
        assertNull(userService.getUserEntityByName("#2"));
        assertNull(userService.getUserEntityByName(""));
    }

    @Test
    void register() {
        when(userRepository.save(any())).thenAnswer(m -> {
            UserEntity userEntity = m.getArgument(0);
            if (userEntity != null && "existing@email.com".equals(userEntity.getEmail())) {
                throw new DataIntegrityViolationException("");
            }
            return userEntity;
        });

        User user = userService.register("name", "email@domain.com", "password", Currency.USD);
        assertEquals("name", user.getName());
        assertEquals("email@domain.com", user.getEmail());
        assertTrue(passwordEncoder.matches("password", user.getEncodedPassword()));
        assertEquals(Currency.USD, user.getDefaultCurrency());
        verify(userRepository, times(1)).save(any());

        assertThrows(IllegalNameException.class, () -> userService.register("", "email@domain.com", "password", Currency.USD));
        assertThrows(IllegalEmailException.class, () -> userService.register("name", "xxx", "password", Currency.USD));
        assertThrows(EmailAlreadyRegisteredException.class, () -> userService.register("name", "existing@email.com", "password", Currency.USD));
        assertThrows(TooShortPasswordException.class, () -> userService.register("name", "email@domain.com", "xxx", Currency.USD));
        assertThrows(TooLongPasswordException.class, () -> userService.register("name", "email@domain.com", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", Currency.USD));
    }

    @Test
    void updateEncodedPassword() {
        User user = userMapper.toUser(MockUsers.newUserEntity(1L));
        String newEncodedPassword = "NEW_ENCODED_PASSWORD";
        userService.updateEncodedPassword(user, newEncodedPassword);
        assertEquals(newEncodedPassword, user.getEncodedPassword());
        verify(userRepository, times(1)).updatePassword(user.getId(), newEncodedPassword);
    }

    @Test
    void getUserBalances() {
        // Return empty default balance for new users
        UserEntity user1 = MockUsers.newUserEntity(1L);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        UserBalancesResponse res = userService.getUserBalances(user1.getId());
        assertEquals(
                UserBalancesResponse.builder()
                        .defaultCurrency(user1.getDefaultCurrency())
                        .balances(Collections.singletonList(userBalanceMapper.toUserBalance(MockUsers.newBalance(BigDecimal.ZERO, Currency.USD))))
                        .build(),
                res);

        // Return sorted balance for users with many balances
        for (int i = 0; i < 4; ++i) { // test all sorting branches
            UserEntity user2 = MockUsers.newUserEntity(2L);
            user2.setBalances(Arrays.asList(
                    MockUsers.newBalance(i / 2 == 1 ? BigDecimal.ZERO : new BigDecimal("150"), Currency.USD),
                    MockUsers.newBalance(new BigDecimal("200"), Currency.EUR),
                    MockUsers.newBalance(new BigDecimal("50"), Currency.JPY),
                    MockUsers.newBalance(new BigDecimal("0"), Currency.GBP),
                    MockUsers.newBalance(new BigDecimal("100"), Currency.CAD)
            ));
            if (i % 2 == 1) {
                Collections.reverse(user2.getBalances());
            }
            when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

            res = userService.getUserBalances(user2.getId());
            assertEquals(
                    UserBalancesResponse.builder()
                            .defaultCurrency(user2.getDefaultCurrency())
                            .balances(Arrays.asList(
                                    userBalanceMapper.toUserBalance(MockUsers.newBalance(i / 2 == 1 ? BigDecimal.ZERO : new BigDecimal("150"), Currency.USD)),
                                    userBalanceMapper.toUserBalance(MockUsers.newBalance(new BigDecimal("200"), Currency.EUR)),
                                    userBalanceMapper.toUserBalance(MockUsers.newBalance(new BigDecimal("50"), Currency.JPY)),
                                    userBalanceMapper.toUserBalance(MockUsers.newBalance(new BigDecimal("100"), Currency.CAD))
                            ))
                            .build(),
                    res);
        }

        // Returns null for non-existent user
        assertNull(userService.getUserBalances(3L));
    }

    @Test
    void normalizeName() {
        assertEquals("Mark Elliot zuckerberG", userService.normalizeName("  Mark   Elliot   zuckerberG"));
        assertEquals("Mǎ Huàténg", userService.normalizeName("Mǎ Huàténg "));
        assertEquals("马云", userService.normalizeName("马云"));
        assertNull(userService.normalizeName("     "));
        assertNull(userService.normalizeName("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
        assertEquals("My--Name", userService.normalizeName("My--Name"));
    }

    @Test
    void validateAndNormalizeNewName() {
        assertEquals("Jack O'neil", userService.validateAndNormalizeNewName("Jack O'neil"));
        assertThrows(IllegalNameException.class, () -> userService.validateAndNormalizeNewName("Jack O''neil"));
        assertThrows(IllegalNameException.class, () -> userService.validateAndNormalizeNewName("Jack Oneil'"));
        assertThrows(IllegalNameException.class, () -> userService.validateAndNormalizeNewName("Jack 'Oneil"));
        assertEquals("Jack O-neil", userService.validateAndNormalizeNewName("Jack O-neil"));
        assertThrows(IllegalNameException.class, () -> userService.validateAndNormalizeNewName("Jack O--neil"));
        assertThrows(IllegalNameException.class, () -> userService.validateAndNormalizeNewName("Jack Oneil-"));
        assertThrows(IllegalNameException.class, () -> userService.validateAndNormalizeNewName("Jack -Oneil"));
        assertThrows(IllegalNameException.class, () -> userService.validateAndNormalizeNewName("Jack O'-neil"));
    }

    @Test
    void normalizeEmail() {
        assertEquals("user@domain.tld", userService.normalizeEmail("user@domain.tld"));
        assertEquals("马云@domain.tld", userService.normalizeEmail("马云@domain.tld"));
        assertEquals("马云@xn--9kq326p.ws", userService.normalizeEmail("马云@马云.ws"));
        assertNull(userService.normalizeEmail("user@\u0080\u0081.ws"));
        assertNull(userService.normalizeEmail("user"));
        assertNull(userService.normalizeEmail("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx@domain.com"));
    }

    @Test
    void validateAndNormalizeNewEmail() {
        assertEquals("user@domain.com", userService.validateAndNormalizeNewEmail("user@domain.com"));
        assertThrows(IllegalEmailException.class, () -> userService.validateAndNormalizeNewEmail("user@domain.tld"));
    }

    @Test
    void validateNewPassword() {
        userService.validateNewPassword("xxxxxxxxxx");
        assertThrows(TooShortPasswordException.class, () -> userService.validateNewPassword("xxx"));
        assertThrows(TooLongPasswordException.class, () -> userService.validateNewPassword("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"));
    }
}