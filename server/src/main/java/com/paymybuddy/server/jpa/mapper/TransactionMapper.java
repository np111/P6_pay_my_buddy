package com.paymybuddy.server.jpa.mapper;

import com.paymybuddy.api.model.transaction.Transaction;
import com.paymybuddy.server.jpa.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.annotation.Scope;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
@Scope("singleton")
public interface TransactionMapper {
    @Mapping(target = "sender", qualifiedByName = "toContact")
    @Mapping(target = "recipient", qualifiedByName = "toContact")
    Transaction toTransaction(TransactionEntity transactionEntity);
}
