package com.paymybuddy.business;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.collection.CursorResponse;
import com.paymybuddy.api.model.transaction.Transaction;
import com.paymybuddy.business.exception.NotEnoughFundsException;
import com.paymybuddy.business.exception.RecipientNotFoundException;
import com.paymybuddy.business.exception.SenderNotFoundException;
import com.paymybuddy.business.mapper.TransactionMapperImpl;
import com.paymybuddy.business.mapper.UserBalanceMapperImpl;
import com.paymybuddy.business.mapper.UserMapper;
import com.paymybuddy.business.mapper.UserMapperImpl;
import com.paymybuddy.business.mock.MockUsers;
import com.paymybuddy.business.mock.TestBusinessConfig;
import com.paymybuddy.business.pageable.CursorRequest;
import com.paymybuddy.persistence.entity.TransactionEntity;
import com.paymybuddy.persistence.entity.UserBalanceEntity;
import com.paymybuddy.persistence.entity.UserEntity;
import com.paymybuddy.persistence.repository.TransactionRepository;
import com.paymybuddy.persistence.repository.UserBalanceRepository;
import com.paymybuddy.persistence.repository.UserRepository;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TransactionService.class, UserService.class, UserMapperImpl.class, UserBalanceMapperImpl.class, TransactionMapperImpl.class})
@Import(TestBusinessConfig.class)
class TransactionServiceTest {
    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private UserBalanceRepository userBalanceRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TransactionService transactionService;

    @Test
    void listTransactions() {
        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(m -> {
            Pageable pageable = m.getArgument(1);
            List<TransactionEntity> content = LongStream.range(0, pageable.getPageSize()).boxed()
                    .map(id -> {
                        TransactionEntity e = new TransactionEntity();
                        e.setId(id);
                        return e;
                    })
                    .collect(Collectors.toList());
            return new PageImpl<>(content, pageable, pageable.getPageSize() * 2L + 3L);
        });

        CursorRequest req = new CursorRequest();
        req.setPageSize(5);
        req.setPageSort(Arrays.asList("-amount", "id"));

        CursorResponse<Transaction> res = transactionService.listTransactions(1L, req);
        assertEquals(false, res.getHasPrev());
        assertEquals("b$.AAAAAAAAAAA", res.getPrevCursor());
        assertEquals(true, res.getHasNext());
        assertEquals("a$.AAAAAAAAAAQ", res.getNextCursor());
        assertEquals(5, res.getRecords().size());
    }

    @Test
    void createTransaction() {
        long senderId = 1L;
        long recipientId = 2L;
        Currency currency = Currency.USD;
        BigDecimal amount = new BigDecimal("1234.56");
        String description = "Hello World";
        BigDecimal fee = transactionService.computeFee(currency, amount);
        ZonedDateTime date = ZonedDateTime.of(2020, 9, 1, 16, 30, 15, 0, ZoneId.of("Europe/Paris"));

        UserEntity sender = MockUsers.newUserEntity(senderId);
        UserEntity recipient = MockUsers.newUserEntity(recipientId);
        when(userRepository.findAllByIdsForUpdate(any())).thenAnswer(m -> {
            Collection<Long> ids = m.getArgument(0);
            return Stream.of(
                    ids.contains(sender.getId()) ? sender : null,
                    ids.contains(recipient.getId()) ? recipient : null
            ).filter(Objects::nonNull).collect(Collectors.toList());
        });

        Table<Long, Currency, UserBalanceEntity> balances = HashBasedTable.create();
        balances.put(senderId, currency, MockUsers.newBalance(new BigDecimal("1000000"), currency, sender));
        when(userBalanceRepository.findByUserIdAndCurrency(anyLong(), any())).thenAnswer(m -> {
            return Optional.ofNullable(balances.get(m.<Long>getArgument(0), m.<Currency>getArgument(1)));
        });
        when(userBalanceRepository.saveAll(any())).thenAnswer(m -> {
            List<UserBalanceEntity> ret = new ArrayList<>();
            m.<Iterable<UserBalanceEntity>>getArgument(0).forEach(e -> {
                e.setUser(Iterables.getFirst(userRepository.findAllByIdsForUpdate(Collections.singleton(e.getUserId())), null));
                balances.put(e.getUserId(), e.getCurrency(), e);
                ret.add(e);
            });
            return ret;
        });

        // Check createTransaction fee overload
        TransactionService transactionServiceSpy = Mockito.spy(transactionService);
        transactionServiceSpy.createTransaction(senderId, recipientId, currency, amount, description, date);
        verify(transactionServiceSpy, times(1)).createTransaction(senderId, recipientId, currency, amount, description, fee, date);

        // Validate the amount
        transactionService.createTransaction(senderId, recipientId, currency, new BigDecimal("10.000"), description, fee, date);
        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(senderId, recipientId, currency, new BigDecimal("10.001"), description, fee, date));
        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(senderId, recipientId, currency, new BigDecimal("0"), description, fee, date));
        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(senderId, recipientId, currency, new BigDecimal("-1"), description, fee, date));

        // Validate the fee
        transactionService.createTransaction(senderId, recipientId, currency, amount, description, new BigDecimal("10.000"), date);
        transactionService.createTransaction(senderId, recipientId, currency, amount, description, new BigDecimal("0"), date);
        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(senderId, recipientId, currency, amount, description, new BigDecimal("10.001"), date));
        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(senderId, recipientId, currency, amount, description, new BigDecimal("-1"), date));

        // Check that the user is not sending to himself
        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(senderId, senderId, currency, amount, description, fee, date));

        // Check SenderNotFoundException/RecipientNotFoundException
        assertThrows(SenderNotFoundException.class, () -> transactionService.createTransaction(3L, recipientId, currency, amount, description, fee, date));
        assertThrows(RecipientNotFoundException.class, () -> transactionService.createTransaction(senderId, 3L, currency, amount, description, fee, date));

        // Check NotEnoughFundsException
        assertThrows(NotEnoughFundsException.class, () -> transactionService.createTransaction(senderId, recipientId, currency, new BigDecimal("100000000.00"), description, fee, date));

        // Check success
        for (int i = 0; i < 2; ++i) {
            balances.clear();
            balances.put(senderId, currency, MockUsers.newBalance(new BigDecimal("1000000"), currency, sender));
            if (i == 0) {
                balances.put(recipientId, currency, MockUsers.newBalance(new BigDecimal("1000000"), currency, recipient));
            }

            Transaction res = transactionService.createTransaction(senderId, recipientId, currency, amount, description, fee, date);
            assertEquals(userMapper.toContact(sender), res.getSender());
            assertEquals(userMapper.toContact(recipient), res.getRecipient());
            assertEquals(currency, res.getCurrency());
            assertEquals(amount, res.getAmount());
            assertEquals(fee, res.getFee());
            assertEquals(description, res.getDescription());
            assertEquals(date, res.getDate());

            assertEquals(ImmutableMap.of(Currency.USD, MockUsers.newBalance(new BigDecimal("998759.26"), currency, sender)), balances.row(senderId));
            assertEquals(ImmutableMap.of(Currency.USD, MockUsers.newBalance(i == 0 ? new BigDecimal("1001234.56") : amount, currency, recipient)), balances.row(recipientId));
        }
    }

    @Test
    void withdrawToBank() {
        long userId = 1L;
        Currency currency = Currency.USD;
        BigDecimal amount = new BigDecimal("1234.56");
        String  iban = "NL91ABNA0417164300";

        UserEntity user = MockUsers.newUserEntity(userId);
        when(userRepository.findAllByIdsForUpdate(any())).thenAnswer(m -> {
            Collection<Long> ids = m.getArgument(0);
            return Stream.of(
                    ids.contains(user.getId()) ? user : null
            ).filter(Objects::nonNull).collect(Collectors.toList());
        });

        Table<Long, Currency, UserBalanceEntity> balances = HashBasedTable.create();
        balances.put(user.getId(), currency, MockUsers.newBalance(new BigDecimal("1000000"), currency, user));
        when(userBalanceRepository.findByUserIdAndCurrency(anyLong(), any())).thenAnswer(m -> {
            return Optional.ofNullable(balances.get(m.<Long>getArgument(0), m.<Currency>getArgument(1)));
        });
        when(userBalanceRepository.saveAll(any())).thenAnswer(m -> {
            List<UserBalanceEntity> ret = new ArrayList<>();
            m.<Iterable<UserBalanceEntity>>getArgument(0).forEach(e -> {
                e.setUser(Iterables.getFirst(userRepository.findAllByIdsForUpdate(Collections.singleton(e.getUserId())), null));
                balances.put(e.getUserId(), e.getCurrency(), e);
                ret.add(e);
            });
            return ret;
        });

        // Validate the amount
        transactionService.withdrawToBank(userId, currency, new BigDecimal("10.000"), iban);
        assertThrows(IllegalArgumentException.class, () -> transactionService.withdrawToBank(userId, currency, new BigDecimal("10.001"), iban));
        assertThrows(IllegalArgumentException.class, () -> transactionService.withdrawToBank(userId, currency, new BigDecimal("0"), iban));
        assertThrows(IllegalArgumentException.class, () -> transactionService.withdrawToBank(userId, currency, new BigDecimal("-1"), iban));

        // Check SenderNotFoundException
        assertThrows(SenderNotFoundException.class, () -> transactionService.withdrawToBank(3L, currency, amount, iban));

        // Check NotEnoughFundsException
        assertThrows(NotEnoughFundsException.class, () -> transactionService.withdrawToBank(userId, currency, new BigDecimal("100000000.00"), iban));

        // Check success
        balances.clear();
        balances.put(userId, currency, MockUsers.newBalance(new BigDecimal("1000000"), currency, user));

        transactionService.withdrawToBank(userId, currency, amount, iban);
        assertEquals(ImmutableMap.of(Currency.USD, MockUsers.newBalance(new BigDecimal("998765.44"), currency, user)), balances.row(userId));
    }

    @Test
    void computeFee() {
        assertEquals(new BigDecimal("5.00"), transactionService.computeFee(Currency.USD, new BigDecimal("1000")));
        assertEquals(new BigDecimal("0.03"), transactionService.computeFee(Currency.USD, new BigDecimal("4.99")));
        assertEquals(new BigDecimal("0.00"), transactionService.computeFee(Currency.USD, new BigDecimal("0.00")));
        assertEquals(new BigDecimal("0.01"), transactionService.computeFee(Currency.USD, new BigDecimal("0.01")));
        assertEquals(new BigDecimal("0.01"), transactionService.computeFee(Currency.USD, new BigDecimal("0.00001")));
        assertEquals(new BigDecimal("0.01"), transactionService.computeFee(Currency.USD, new BigDecimal("0.33")));

        assertEquals(new BigDecimal("5"), transactionService.computeFee(Currency.JPY, new BigDecimal("1000")));
        assertEquals(new BigDecimal("2"), transactionService.computeFee(Currency.JPY, new BigDecimal("399.9")));
        assertEquals(new BigDecimal("0"), transactionService.computeFee(Currency.JPY, new BigDecimal("0.00")));
        assertEquals(new BigDecimal("1"), transactionService.computeFee(Currency.JPY, new BigDecimal("0.01")));
        assertEquals(new BigDecimal("1"), transactionService.computeFee(Currency.JPY, new BigDecimal("0.00001")));
        assertEquals(new BigDecimal("1"), transactionService.computeFee(Currency.JPY, new BigDecimal("0.33")));
    }
}
