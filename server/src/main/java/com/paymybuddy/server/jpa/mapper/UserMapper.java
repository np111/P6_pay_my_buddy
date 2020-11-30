package com.paymybuddy.server.jpa.mapper;

import com.paymybuddy.api.model.User;
import com.paymybuddy.server.jpa.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.annotation.Scope;

@Mapper(componentModel = "spring")
@Scope("singleton")
public interface UserMapper {
    @Mapping(target = "balances", ignore = true)
    User toUser(UserEntity car);
}
