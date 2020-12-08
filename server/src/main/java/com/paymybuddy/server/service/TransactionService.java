package com.paymybuddy.server.service;

import com.google.common.base.Preconditions;
import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.collection.CursorResponse;
import com.paymybuddy.api.model.transaction.Transaction;
import com.paymybuddy.server.jpa.entity.TransactionEntity;
import com.paymybuddy.server.jpa.entity.UserBalanceEntity;
import com.paymybuddy.server.jpa.entity.UserEntity;
import com.paymybuddy.server.jpa.mapper.TransactionMapper;
import com.paymybuddy.server.jpa.repository.TransactionRepository;
import com.paymybuddy.server.jpa.repository.UserBalanceRepository;
import com.paymybuddy.server.jpa.repository.UserRepository;
import com.paymybuddy.server.jpa.util.CursorFetcher;
import com.paymybuddy.server.util.exception.FastRuntimeException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.paymybuddy.server.jpa.repository.TransactionRepository.isRecipient;
import static com.paymybuddy.server.jpa.repository.TransactionRepository.isSender;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class TransactionService {
    private static final BigDecimal FEE_PERCENTS = new BigDecimal("0.005");

    private final UserRepository userRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public CursorResponse<Transaction> listTransactions(long userId, CursorFetcher.Request cursorRequest) {
        return CursorFetcher.<Transaction, TransactionEntity>create()
                .recordsQuery(q -> transactionRepository.findAll(
                        q.getSpecification().and(isSender(userId).or(isRecipient(userId))),
                        q.getPageable()))
                .recordMapper(transactionMapper::toTransaction)
                .property("id", new CursorFetcher.LongPropertyType(), TransactionEntity::getId, true)
                .property("amount", new CursorFetcher.BigDecimalPropertyType(), TransactionEntity::getAmount)
                .fetch(cursorRequest);
    }

    @Transactional
    public Transaction createTransaction(long senderId, long recipientId, Currency currency, BigDecimal amount, String description, BigDecimal fee, ZonedDateTime date) {
        // Validate the amount
        amount = amount.stripTrailingZeros();
        Preconditions.checkArgument(amount.scale() <= currency.getDecimals(), "amount has too many decimals");
        Preconditions.checkArgument(amount.compareTo(BigDecimal.ZERO) > 0, "amount must be strictly positive");

        // Validate the fee
        fee = fee.stripTrailingZeros();
        Preconditions.checkArgument(fee.scale() <= currency.getDecimals(), "fee has too many decimals");
        Preconditions.checkArgument(fee.compareTo(BigDecimal.ZERO) >= 0, "fee must be positive");

        // Check that the user is not sending to himself
        Preconditions.checkArgument(senderId != recipientId, "senderId and recipientId must be different");

        // Find users and lock them (in a single ordered operation to prevent a "different-order deadlock")
        Map<Long, UserEntity> users = userRepository.findAllByIdsForUpdate(Arrays.asList(senderId, recipientId))
                .stream().collect(Collectors.toMap(UserEntity::getId, u -> u));
        UserEntity sender = users.get(senderId);
        UserEntity recipient = users.get(recipientId);
        if (sender == null) {
            throw new SenderNotFoundException();
        }
        if (recipient == null) {
            throw new RecipientNotFoundException();
        }

        // Withdraw the sender amount
        UserBalanceEntity senderBalance = getBalance(sender.getId(), currency);
        senderBalance.setAmount(senderBalance.getAmount().subtract(amount).subtract(fee));
        if (senderBalance.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughFundsException(currency, senderBalance.getAmount().abs());
        }

        // Add the recipient amount
        UserBalanceEntity recipientBalance = getBalance(recipient.getId(), currency);
        recipientBalance.setAmount(recipientBalance.getAmount().add(amount));

        // And create the transaction entry
        TransactionEntity transaction = new TransactionEntity();
        transaction.setSenderId(sender.getId());
        transaction.setRecipientId(recipient.getId());
        transaction.setCurrency(currency);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setDescription(description);
        transaction.setDate(date);

        transactionRepository.save(transaction);
        userBalanceRepository.save(senderBalance);
        userBalanceRepository.save(recipientBalance);

        transaction.setSender(sender);
        transaction.setRecipient(recipient);
        return transactionMapper.toTransaction(transaction);
    }

    public BigDecimal computeFee(Currency currency, BigDecimal amount) {
        return amount.multiply(FEE_PERCENTS).setScale(currency.getDecimals(), RoundingMode.UP);
    }

    private UserBalanceEntity getBalance(long userId, Currency currency) {
        UserBalanceEntity userBalanceEntity = userBalanceRepository.findByUserIdAndCurrency(userId, currency).orElse(null);
        if (userBalanceEntity == null) {
            userBalanceEntity = new UserBalanceEntity();
            userBalanceEntity.setUserId(userId);
            userBalanceEntity.setCurrency(currency);
            userBalanceEntity.setAmount(new BigDecimal(0));
        }
        return userBalanceEntity;
    }

    public static class SenderNotFoundException extends FastRuntimeException {
    }

    public static class RecipientNotFoundException extends FastRuntimeException {
    }

    @Getter
    public static class NotEnoughFundsException extends FastRuntimeException {
        private final Currency currency;
        private final BigDecimal missingAmount;

        public NotEnoughFundsException(Currency currency, BigDecimal missingAmount) {
            this.currency = currency;
            this.missingAmount = missingAmount;
        }
    }
}
