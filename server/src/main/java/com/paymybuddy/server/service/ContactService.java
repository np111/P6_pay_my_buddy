package com.paymybuddy.server.service;

import com.paymybuddy.api.model.collection.PageableResponse;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.server.jpa.entity.UserContactEntity;
import com.paymybuddy.server.jpa.entity.UserEntity;
import com.paymybuddy.server.jpa.mapper.UserMapper;
import com.paymybuddy.server.jpa.repository.UserContactRepository;
import com.paymybuddy.server.jpa.repository.UserRepository;
import com.paymybuddy.server.jpa.util.PageableFetcher;
import com.paymybuddy.server.util.exception.FastRuntimeException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
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
    public PageableResponse<User> listContacts(long userId, PageableFetcher.Params pageableParams) {
        return PageableFetcher.<User>create()
                .recordsFinder(pageable -> userContactRepository.findContactByUserId(userId, pageable)
                        .stream().map(userMapper::toUser).collect(Collectors.toList()))
                .recordsCounter(() -> userContactRepository.countByUserId(userId))
                .minPerPage(1) // FIXME: Allow low values for tests/examples
                .defaultSort(Sort.by("contact.name"))
                .sortablePropertyTransformer(p -> "contact." + p)
                .sortableProperty("name")
                .sortableProperty("email")
                .fetch(pageableParams);
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
        return userMapper.toUser(contactEntity);
    }

    @Transactional
    public User removeContact(long userId, Long contactId) {
        UserContactEntity contactEntryEntity = userContactRepository.findById(new UserContactEntity.Key(userId, contactId)).orElse(null);
        if (contactEntryEntity == null) {
            throw new ContactNotFoundException();
        }

        User contact = userMapper.toUser(contactEntryEntity.getContact());
        userContactRepository.delete(contactEntryEntity);
        return contact;
    }

    public static class ContactNotFoundException extends FastRuntimeException {
    }
}
