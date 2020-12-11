package com.paymybuddy.persistence.repository;

import com.paymybuddy.persistence.entity.UserEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    @Modifying
    @Query("update UserEntity u set u.encodedPassword = :encodedPassword where u.id = :id")
    void updatePassword(@Param("id") long id, @Param("encodedPassword") String encodedPassword);

    @Query("SELECT u FROM UserEntity u WHERE u.id IN :ids ORDER BY u.id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<UserEntity> findAllByIdsForUpdate(@Param("ids") Collection<Long> ids);
}
