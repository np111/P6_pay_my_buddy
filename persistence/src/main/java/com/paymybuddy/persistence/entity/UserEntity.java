package com.paymybuddy.persistence.entity;

import com.paymybuddy.api.model.Currency;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing a user.
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
@Data
@ToString(exclude = {"balances"})
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email")
    private String email;

    /**
     * Bcrypt encoded password (a "$2" string).
     */
    @Column(name = "password", columnDefinition = "CHAR", length = 60)
    private String encodedPassword;

    @Column(name = "name")
    private String name;

    @Column(name = "default_currency", columnDefinition = "CHAR", length = 3)
    @Enumerated(EnumType.STRING)
    private Currency defaultCurrency;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userId")
    private List<UserBalanceEntity> balances;
}
