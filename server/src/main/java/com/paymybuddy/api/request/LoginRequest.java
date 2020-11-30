package com.paymybuddy.api.request;

import com.paymybuddy.api.validation.constraint.IsEmail;
import com.paymybuddy.api.validation.constraint.IsPassword;
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
public class LoginRequest {
    @NotNull
    @IsEmail
    private String email;

    @NotNull
    @IsPassword
    private String password;
}
