package com.paymybuddy.business;

import com.paymybuddy.api.model.collection.ListResponse;
import com.paymybuddy.api.model.collection.PageResponse;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.business.exception.ContactNotFoundException;
import com.paymybuddy.business.exception.IsHimselfException;
import com.paymybuddy.business.mapper.UserBalanceMapperImpl;
import com.paymybuddy.business.mapper.UserMapper;
import com.paymybuddy.business.mapper.UserMapperImpl;
import com.paymybuddy.business.mock.MockUsers;
import com.paymybuddy.business.mock.TestBusinessConfig;
import com.paymybuddy.persistence.entity.UserContactEntity;
import com.paymybuddy.persistence.entity.UserEntity;
import com.paymybuddy.persistence.repository.UserContactRepository;
import com.paymybuddy.persistence.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ContactService.class, UserService.class, UserMapperImpl.class, UserBalanceMapperImpl.class})
@Import(TestBusinessConfig.class)
class ContactServiceTest {
    @MockBean
    private UserContactRepository userContactRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ContactService contactService;

    @Test
    void listContacts() {
        when(userContactRepository.findContactByUserId(eq(1L), any())).thenAnswer(m -> {
            Pageable pageable = m.getArgument(1);
            List<UserEntity> content = IntStream.range(0, pageable.getPageSize()).boxed()
                    .map(id -> MockUsers.newUserEntity(id + 10L))
                    .collect(Collectors.toList());
            return new PageImpl<>(content, pageable, pageable.getPageSize() * 2L + 3L);
        });

        com.paymybuddy.business.pageable.PageRequest req = new com.paymybuddy.business.pageable.PageRequest();
        req.setPage(2);
        req.setPageSize(5);
        req.setPageSort(Arrays.asList("-email", "name"));
        PageResponse<User> res = contactService.listContacts(1L, req);
        assertEquals(2, res.getPage());
        assertEquals(5, res.getPageSize());
        assertEquals(3, res.getPageCount());
        assertEquals(15, res.getTotalCount());
        assertEquals(5, res.getRecords().size());
    }

    @Test
    void searchContacts() {
        ListResponse<User> res = contactService.searchContacts(1L, "t_e%s_t%", 5);
        assertTrue(res.getRecords().isEmpty());
        verify(userContactRepository, times(1)).searchContact(1L, "%t\\_e\\%s\\_t\\%%", PageRequest.of(0, 5));
    }

    @Test
    void isContact() {
        when(userContactRepository.existsById(new UserContactEntity.Key(1L, 2L))).thenReturn(true);
        assertTrue(contactService.isContact(1L, 2L));
        assertFalse(contactService.isContact(1L, 3L));
    }

    @Test
    void addContact() {
        UserEntity user1 = MockUsers.newUserEntity(1L);
        UserEntity user2 = MockUsers.newUserEntity(2L);
        when(userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.of(user1));
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(Optional.of(user2));

        assertEquals(userMapper.toContact(user2), contactService.addContact(user1.getId(), user2.getEmail()));
        assertThrows(ContactNotFoundException.class, () -> contactService.addContact(user1.getId(), "unknown@domain.com"));
        assertThrows(IsHimselfException.class, () -> contactService.addContact(user1.getId(), user1.getEmail()));
    }

    @Test
    void removeContact() {
        UserEntity user1 = MockUsers.newUserEntity(1L);
        UserEntity user2 = MockUsers.newUserEntity(2L);
        when(userContactRepository.findById(new UserContactEntity.Key(user1.getId(), user2.getId()))).thenAnswer(m -> {
            UserContactEntity e = new UserContactEntity();
            e.setUser(user1);
            e.setContact(user2);
            return Optional.of(e);
        });

        assertEquals(userMapper.toContact(user2), contactService.removeContact(user1.getId(), user2.getId()));
        assertThrows(ContactNotFoundException.class, () -> contactService.removeContact(user1.getId(), 3L));
    }

    @Test
    void getContact() {
        UserEntity user1 = MockUsers.newUserEntity(1L);
        UserEntity user2 = MockUsers.newUserEntity(2L);
        when(userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.of(user1));
        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));
        when(userRepository.findByEmail(user2.getEmail())).thenReturn(Optional.of(user2));
        when(userRepository.findByName(user2.getName())).thenReturn(Optional.of(user2));
        when(userContactRepository.existsById(new UserContactEntity.Key(user1.getId(), user2.getId()))).thenReturn(true);

        assertEquals(userMapper.toContact(user2), contactService.getContact(user1.getId(), user2.getEmail()));
        assertEquals(userMapper.toContact(user2), contactService.getContact(user1.getId(), user2.getName()));
        assertNull(contactService.getContact(user1.getId(), user1.getEmail()));
        assertNull(contactService.getContact(user1.getId(), user1.getName()));
        assertNull(contactService.getContact(user1.getId(), "unknown@email.com"));
        assertNull(contactService.getContact(user1.getId(), "unknown"));
    }
}