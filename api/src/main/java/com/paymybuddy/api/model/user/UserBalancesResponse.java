package com.paymybuddy.api.model.user;

import com.paymybuddy.api.model.Currency;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class UserBalancesResponse {
    private Currency defaultCurrency;

    private List<UserBalance> balances;
}
