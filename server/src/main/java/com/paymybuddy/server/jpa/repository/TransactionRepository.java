package com.paymybuddy.server.jpa.repository;

import com.paymybuddy.server.jpa.entity.TransactionEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long>, JpaSpecificationExecutor<TransactionEntity> {
    static Specification<TransactionEntity> isSender(long userId) {
        return (root, query, builder) -> builder.equal(root.get("senderId"), userId);
    }

    static Specification<TransactionEntity> isRecipient(long userId) {
        return (root, query, builder) -> builder.equal(root.get("recipientId"), userId);
    }
}
