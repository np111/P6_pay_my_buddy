package com.paymybuddy.server.jpa.entity;

import com.paymybuddy.api.model.Currency;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "user_balances")
@IdClass(UserBalanceEntity.Key.class)
@NoArgsConstructor
@Data
public class UserBalanceEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Id
    @Column(name = "currency", columnDefinition = "CHAR", length = 3)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "amount", precision = 40, scale = 20)
    private BigDecimal amount;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static final class Key implements Serializable {
        private Long userId;
        private Currency currency;
    }
}
