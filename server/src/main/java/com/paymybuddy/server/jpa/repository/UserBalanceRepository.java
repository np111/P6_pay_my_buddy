package com.paymybuddy.server.jpa.repository;

import com.paymybuddy.api.model.Currency;
import com.paymybuddy.server.jpa.entity.UserBalanceEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBalanceRepository extends JpaRepository<UserBalanceEntity, UserBalanceEntity.Key> {
    List<UserBalanceEntity> findByUserId(Long userId);

    Optional<UserBalanceEntity> findByUserIdAndCurrency(Long userId, Currency currency);
}
