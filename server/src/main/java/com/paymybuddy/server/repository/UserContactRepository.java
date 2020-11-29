package com.paymybuddy.server.repository;

import com.paymybuddy.server.entity.UserContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserContactRepository extends JpaRepository<UserContactEntity, UserContactEntity.Key> {
}
