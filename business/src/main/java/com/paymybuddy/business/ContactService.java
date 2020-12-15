package com.paymybuddy.business;

import com.paymybuddy.api.model.collection.ListResponse;
import com.paymybuddy.api.model.collection.PageResponse;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.business.exception.ContactNotFoundException;
import com.paymybuddy.business.exception.IsHimselfException;
import com.paymybuddy.business.mapper.UserMapper;
import com.paymybuddy.business.pageable.PageFetcher;
import com.paymybuddy.business.pageable.PageRequest;
import com.paymybuddy.persistence.entity.UserContactEntity;
import com.paymybuddy.persistence.entity.UserEntity;
import com.paymybuddy.persistence.repository.UserContactRepository;
import com.paymybuddy.persistence.util.JpaUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contacts management service.
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Scope("singleton")
public class ContactService {
    private final UserService userService;
    private final UserContactRepository userContactRepository;
    private final UserMapper userMapper;

    /**
     * List a user's contacts.
     *
     * @param userId      ID of the user to returns contacts
     * @param pageRequest pagination parameters
     * @return the contacts list
     */
    @Transactional(readOnly = true)
    public PageResponse<User> listContacts(long userId, PageRequest pageRequest) {
        return PageFetcher.<User, UserEntity>create()
                .recordsQuery(pageable -> userContactRepository.findContactByUserId(userId, pageable))
                .recordMapper(userMapper::toContact)
                .sortPropertyTransformer(p -> "contact." + p)
                .fetch(pageRequest);
    }

    /**
     * Search for user's contacts.
     * <p>
     * Search is performed on names and emails.
     *
     * @param userId ID of the user to search contacts
     * @param input  Term to look for
     * @param limit  Maximum number of results
     * @return the matching contacts list (sorted from best matching to the less)
     */
    @Transactional(readOnly = true)
    public ListResponse<User> searchContacts(long userId, String input, int limit) {
        String jpaInput = '%' + JpaUtil.escapeLikeParam(input) + '%'; // transform the input for the LIKE statement

        List<User> ret = userContactRepository.searchContact(userId, jpaInput, org.springframework.data.domain.PageRequest.of(0, limit))
                .stream()
                .map(userMapper::toContact)
                .collect(Collectors.toList());
        // TODO: sort by best matching
        return ListResponse.of(ret);
    }

    /**
     * Checks if a user has another user in his contact list.
     *
     * @param userId    ID of the user
     * @param contactId ID of the contact to check
     * @return Whether or not users are in contact
     */
    @Transactional(readOnly = true)
    public boolean isContact(long userId, long contactId) {
        return userContactRepository.existsById(new UserContactEntity.Key(userId, contactId));
    }

    /**
     * Add a new contact.
     * <p>
     * If the contact already exists, no error is returned.
     *
     * @param userId       ID of the user
     * @param contactEmail Email of the user to add as contact
     * @return the contact
     * @throws ContactNotFoundException if no users match the contactEmail
     * @throws IsHimselfException       if the contact is equals to the user
     */
    @Transactional
    public User addContact(long userId, String contactEmail) {
        UserEntity contactEntity = userService.getUserEntityByEmail(contactEmail);
        if (contactEntity == null) {
            throw new ContactNotFoundException();
        }
        if (userId == contactEntity.getId()) {
            throw new IsHimselfException();
        }

        UserContactEntity contactEntryEntity = new UserContactEntity();
        contactEntryEntity.setUserId(userId);
        contactEntryEntity.setContactId(contactEntity.getId());
        userContactRepository.save(contactEntryEntity);
        return userMapper.toContact(contactEntity);
    }

    /**
     * Removes an existing contact.
     *
     * @param userId    ID of the user
     * @param contactId ID of the contact to remove
     * @return the removed contact
     * @throws ContactNotFoundException if the contact does not exists
     */
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

    /**
     * Find an existing contact by it's email or name.
     *
     * @param userId          ID of the user
     * @param contactSelector email or name of the contact
     * @return the contact; or {@code null} if none match
     */
    @Transactional(readOnly = true)
    public User getContact(long userId, String contactSelector) {
        UserEntity contactEntity = userService.getUserEntityByEmail(contactSelector);
        if (contactEntity == null) {
            contactEntity = userService.getUserEntityByName(contactSelector);
        }
        if (contactEntity == null || !isContact(userId, contactEntity.getId())) {
            return null;
        }
        return userMapper.toContact(contactEntity);
    }
}
