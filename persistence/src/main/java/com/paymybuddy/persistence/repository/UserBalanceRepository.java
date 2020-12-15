package com.paymybuddy.persistence.repository;

import com.paymybuddy.api.model.Currency;
import com.paymybuddy.persistence.entity.UserBalanceEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalanceEntity, UserBalanceEntity.Key> {
    Optional<UserBalanceEntity> findByUserIdAndCurrency(Long userId, Currency currency);
}
