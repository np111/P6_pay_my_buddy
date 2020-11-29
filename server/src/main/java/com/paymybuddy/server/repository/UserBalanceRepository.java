package com.paymybuddy.server.repository;

import com.paymybuddy.server.entity.UserBalanceEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBalanceRepository extends JpaRepository<UserBalanceEntity, UserBalanceEntity.Key> {
    List<UserBalanceEntity> findByUserId(Long userId);

    Optional<UserBalanceEntity> findByUserIdAndCurrency(Long userId, String currency);
}
