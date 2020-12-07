package com.paymybuddy.api.request.auth;

import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.util.validation.constraint.IsEmail;
import com.paymybuddy.api.util.validation.constraint.IsName;
import com.paymybuddy.api.util.validation.constraint.IsPassword;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class RegisterRequest {
    @NotNull
    @IsName
    private String name;

    @NotNull
    @IsEmail
    private String email;

    @NotNull
    @IsPassword
    private String password;

    @NotNull
    private Currency defaultCurrency;
}
