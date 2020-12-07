package com.paymybuddy.server.jpa.entity;

import com.paymybuddy.api.model.Currency;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@Data
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sender_id")
    private Long senderId;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private UserEntity sender;

    @Column(name = "recipient_id")
    private Long recipientId;

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", insertable = false, updatable = false)
    private UserEntity recipient;

    @Column(name = "currency", columnDefinition = "CHAR", length = 3)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "amount", precision = 40, scale = 20)
    private BigDecimal amount;

    @Column(name = "fee", precision = 40, scale = 20)
    private BigDecimal fee;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "date")
    private ZonedDateTime date;
}
