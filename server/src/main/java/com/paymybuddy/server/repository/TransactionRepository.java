package com.paymybuddy.server.repository;

import com.paymybuddy.server.entity.TransactionEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    @Query("SELECT t FROM TransactionEntity t WHERE (t.senderId = :userId OR t.recipientId = :userId) ORDER BY t.date DESC")
    List<TransactionEntity> findByUserId(Long userId, Pageable pageable);
}
