package com.paymybuddy.server.jpa.mapper;

import com.paymybuddy.api.model.user.User;
import com.paymybuddy.server.config.MapperConfig;
import com.paymybuddy.server.jpa.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.context.annotation.Scope;

@Mapper(config = MapperConfig.class)
@Scope("singleton")
public interface UserMapper {
    @Named("toUser")
    User toUser(UserEntity userEntity);

    @Named("toContact")
    User toContact(UserEntity userEntity);
}
