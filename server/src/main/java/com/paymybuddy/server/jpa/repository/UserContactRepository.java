package com.paymybuddy.server.jpa.repository;

import com.paymybuddy.server.jpa.entity.UserContactEntity;
import com.paymybuddy.server.jpa.entity.UserEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
public interface UserContactRepository extends JpaRepository<UserContactEntity, UserContactEntity.Key> {
    long countByUserId(long userId);

    @Query("SELECT c.contact FROM UserContactEntity AS c WHERE c.userId = :userId")
    List<UserEntity> findContactByUserId(@Param("userId") long userId, Pageable pageable);
}
