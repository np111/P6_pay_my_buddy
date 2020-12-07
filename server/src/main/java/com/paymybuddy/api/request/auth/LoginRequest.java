package com.paymybuddy.api.request.auth;

import com.paymybuddy.api.util.validation.constraint.IsEmail;
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
public class LoginRequest {
    @NotNull
    @IsEmail
    private String email;

    @NotNull
    @IsPassword
    private String password;
}
