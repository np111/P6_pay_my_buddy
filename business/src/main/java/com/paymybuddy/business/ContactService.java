package com.paymybuddy.business;

import com.paymybuddy.api.model.collection.PageResponse;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.business.fetcher.PageFetcher;
import com.paymybuddy.persistence.entity.UserContactEntity;
import com.paymybuddy.persistence.entity.UserEntity;
import com.paymybuddy.persistence.mapper.UserMapper;
import com.paymybuddy.persistence.repository.UserContactRepository;
import com.paymybuddy.persistence.repository.UserRepository;
import com.paymybuddy.business.exception.FastRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class ContactService {
    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public PageResponse<User> listContacts(long userId, PageFetcher.Request pageRequest) {
        return PageFetcher.<User, UserEntity>create()
                .recordsQuery(pageable -> userContactRepository.findContactByUserId(userId, pageable))
                .recordMapper(userMapper::toContact)
                .sortPropertyTransformer(p -> "contact." + p)
                .fetch(pageRequest);
    }

    @Transactional(readOnly = true)
    public boolean isContact(long userId, long contactId) {
        return userContactRepository.existsById(new UserContactEntity.Key(userId, contactId));
    }

    @Transactional
    public User addContact(long userId, String contactEmail) {
        UserEntity contactEntity = userRepository.findByEmail(contactEmail).orElse(null);
        if (contactEntity == null) {
            throw new ContactNotFoundException();
        }

        UserContactEntity contactEntryEntity = new UserContactEntity();
        contactEntryEntity.setUserId(userId);
        contactEntryEntity.setContactId(contactEntity.getId());
        userContactRepository.save(contactEntryEntity);
        return userMapper.toContact(contactEntity);
    }

    @Transactional
    public User removeContact(long userId, long contactId) {
        UserContactEntity contactEntryEntity = userContactRepository.findById(new UserContactEntity.Key(userId, contactId)).orElse(null);
        if (contactEntryEntity == null) {
            throw new ContactNotFoundException();
        }

        User contact = userMapper.toContact(contactEntryEntity.getContact());
        userContactRepository.delete(contactEntryEntity);
        return contact;
    }

    public static class ContactNotFoundException extends FastRuntimeException {
    }
}
