package com.paymybuddy.api.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paymybuddy.api.model.Currency;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class User {
    private Long id;

    private String email;

    @JsonIgnore
    private String encodedPassword;

    private String name;

    private Currency defaultCurrency;
}
