package com.paymybuddy.persistence.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_contacts")
@IdClass(UserContactEntity.Key.class)
@NoArgsConstructor
@Data
public class UserContactEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Id
    @Column(name = "contact_id")
    private Long contactId;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", insertable = false, updatable = false)
    private UserEntity contact;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static final class Key implements Serializable {
        private Long userId;
        private Long contactId;
    }
}
