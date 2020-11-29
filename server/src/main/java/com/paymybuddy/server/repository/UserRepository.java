package com.paymybuddy.server.repository;

import com.paymybuddy.server.entity.UserEntity;
import java.util.Collection;
import java.util.List;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query("SELECT u FROM UserEntity u WHERE u.id IN :ids")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<UserEntity> findAllByIdInForUpdate(Collection<Long> ids);
}
