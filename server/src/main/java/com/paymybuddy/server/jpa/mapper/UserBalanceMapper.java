package com.paymybuddy.server.jpa.mapper;

import com.paymybuddy.api.model.user.UserBalance;
import com.paymybuddy.server.config.MapperConfig;
import com.paymybuddy.server.jpa.entity.UserBalanceEntity;
import org.mapstruct.Mapper;
import org.springframework.context.annotation.Scope;

@Mapper(config = MapperConfig.class)
@Scope("singleton")
public interface UserBalanceMapper {
    UserBalance toUserBalance(UserBalanceEntity userBalanceEntity);
}
