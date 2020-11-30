package com.paymybuddy.server.jpa.repository;

import com.paymybuddy.server.jpa.entity.UserContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserContactRepository extends JpaRepository<UserContactEntity, UserContactEntity.Key> {
}
