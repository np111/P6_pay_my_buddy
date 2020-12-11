package com.paymybuddy.persistence.mapper;

import com.paymybuddy.api.model.transaction.Transaction;
import com.paymybuddy.persistence.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.annotation.Scope;

@Mapper(config = MapperConfig.class, uses = {UserMapper.class})
@Scope("singleton")
public interface TransactionMapper {
    @Mapping(target = "sender", qualifiedByName = "toContact")
    @Mapping(target = "recipient", qualifiedByName = "toContact")
    Transaction toTransaction(TransactionEntity transactionEntity);
}
