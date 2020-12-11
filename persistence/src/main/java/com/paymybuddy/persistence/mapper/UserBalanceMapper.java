package com.paymybuddy.persistence.mapper;

import com.paymybuddy.api.model.user.UserBalance;
import com.paymybuddy.persistence.entity.UserBalanceEntity;
import org.mapstruct.Mapper;
import org.springframework.context.annotation.Scope;

@Mapper(config = MapperConfig.class)
@Scope("singleton")
public interface UserBalanceMapper {
    UserBalance toUserBalance(UserBalanceEntity userBalanceEntity);
}
