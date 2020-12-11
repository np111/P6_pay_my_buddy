package com.paymybuddy.api.request.user;

import com.paymybuddy.api.util.validation.constraint.IsEmail;
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
public class AddContactRequest {
    @NotNull
    @IsEmail
    private String email;
}
