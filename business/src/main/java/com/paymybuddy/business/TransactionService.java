package com.paymybuddy.business;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.collection.CursorResponse;
import com.paymybuddy.api.model.transaction.Transaction;
import com.paymybuddy.business.exception.NotEnoughFundsException;
import com.paymybuddy.business.exception.RecipientNotFoundException;
import com.paymybuddy.business.exception.SenderNotFoundException;
import com.paymybuddy.business.mapper.TransactionMapper;
import com.paymybuddy.business.pageable.CursorFetcher;
import com.paymybuddy.business.pageable.CursorRequest;
import com.paymybuddy.business.pageable.type.BigDecimalPropertyType;
import com.paymybuddy.business.pageable.type.LongPropertyType;
import com.paymybuddy.persistence.entity.TransactionEntity;
import com.paymybuddy.persistence.entity.UserBalanceEntity;
import com.paymybuddy.persistence.entity.UserEntity;
import com.paymybuddy.persistence.repository.TransactionRepository;
import com.paymybuddy.persistence.repository.UserBalanceRepository;
import com.paymybuddy.persistence.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.paymybuddy.persistence.repository.TransactionRepository.isRecipient;
import static com.paymybuddy.persistence.repository.TransactionRepository.isSender;

/**
 * Transactions management service.
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class TransactionService {
    /**
     * Fees charged to the sender during a transactions.
     */
    public static final BigDecimal FEE_PERCENTS = new BigDecimal("0.005");

    private final UserRepository userRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    /**
     * List a user's transactions (where he is a sender or recipient).
     * <p>
     * Sortable properties are:
     * <ul>
     * <li>- id</li>
     * <li>- amount</li>
     * </ul>
     *
     * @param userId        ID of the user to returns transactions
     * @param cursorRequest pagination parameters
     * @return the transaction list
     */
    @Transactional(readOnly = true)
    public CursorResponse<Transaction> listTransactions(long userId, CursorRequest cursorRequest) {
        return CursorFetcher.<Transaction, TransactionEntity>create()
                .recordsQuery(q -> transactionRepository.findAll(
                        q.getSpecification().and(isSender(userId).or(isRecipient(userId))),
                        q.getPageable()))
                .recordMapper(transactionMapper::toTransaction)
                .property("id", new LongPropertyType(), TransactionEntity::getId, true)
                .property("amount", new BigDecimalPropertyType(), TransactionEntity::getAmount)
                .fetch(cursorRequest);
    }

    /**
     * Create a transaction with the default fees.
     *
     * @see #createTransaction(long, long, Currency, BigDecimal, String, BigDecimal, ZonedDateTime)
     */
    @Transactional
    public Transaction createTransaction(long senderId, long recipientId, Currency currency, BigDecimal amount, String description, ZonedDateTime date) {
        BigDecimal fee = computeFee(currency, amount);
        return createTransaction(senderId, recipientId, currency, amount, description, fee, date);
    }

    /**
     * Create a transaction.
     *
     * @param senderId    ID of the user sending the money
     * @param recipientId ID of the user receiving the money
     * @param currency    amount currency
     * @param amount      amount value
     * @param description description of the transaction
     * @param fee         fees to charges to the sender (using the amount currency)
     * @param date        transaction date
     * @return the created transaction
     * @throws IllegalArgumentException   if the amount or fee values have too many decimals for this currency.
     *                                    if the amount value is less or equal to zero.
     *                                    if the fee value is less to zero.
     *                                    if the senderId and recipientId are equals.
     * @throws SenderNotFoundException    if the no user match the senderId
     * @throws RecipientNotFoundException if the no user match the recipientId
     * @throws NotEnoughFundsException    if the sender has not enough funds to cover the amount and fee values (in this currency).
     */
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
                .stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));
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

        // Then explicitly save and returns mapped values
        transactionRepository.save(transaction);
        userBalanceRepository.saveAll(Arrays.asList(senderBalance, recipientBalance));

        transaction.setSender(sender);
        transaction.setRecipient(recipient);
        return transactionMapper.toTransaction(transaction);
    }

    /**
     * Withdraw balance money to a bank account.
     *
     * @param userId   ID of the user withdrawing the money
     * @param currency amount currency
     * @param amount   amount value
     * @param iban     bank account IBAN
     * @throws IllegalArgumentException if the amount value have too many decimals for this currency.
     *                                  if the amount value is less or equal to zero.
     * @throws SenderNotFoundException  if the no user match the userId
     * @throws NotEnoughFundsException  if the sender has not enough funds to cover the amount value (in this currency).
     */
    public boolean withdrawToBank(long userId, Currency currency, BigDecimal amount, String iban) {
        // Validate the amount
        amount = amount.stripTrailingZeros();
        Preconditions.checkArgument(amount.scale() <= currency.getDecimals(), "amount has too many decimals");
        Preconditions.checkArgument(amount.compareTo(BigDecimal.ZERO) > 0, "amount must be strictly positive");

        // Find user and lock him
        UserEntity user = Iterables.getFirst(userRepository.findAllByIdsForUpdate(Collections.singletonList(userId)), null);
        if (user == null) {
            throw new SenderNotFoundException();
        }

        // Withdraw the amount
        UserBalanceEntity userBalance = getBalance(user.getId(), currency);
        userBalance.setAmount(userBalance.getAmount().subtract(amount));
        if (userBalance.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughFundsException(currency, userBalance.getAmount().abs());
        }

        // TODO: Send the money to the `iban` bank account (w/ banking microservice)

        // Then explicitly save and returns mapped values
        userBalanceRepository.save(userBalance);

        return true;
    }

    /**
     * Compute the default fees (using {@link #FEE_PERCENTS}) based on a transaction amount.
     * <p>
     * The result is scaled to the currency decimal places (rounded up if needed).
     *
     * @param currency amount currency
     * @param amount   amount value
     * @return the fee value (using the same currency)
     */
    public BigDecimal computeFee(Currency currency, BigDecimal amount) {
        return amount.multiply(FEE_PERCENTS).setScale(currency.getDecimals(), RoundingMode.UP);
    }

    /**
     * Returns a user's balance in the given currency.
     * <p>
     * If the balance does not exist, it is created.
     *
     * @param userId   ID of the user
     * @param currency currency of the balance to returns
     * @return the balance
     */
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
}
