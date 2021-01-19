package com.paymybuddy.server.http.controller;

import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.api.model.Currency;
import com.paymybuddy.api.model.collection.CursorResponse;
import com.paymybuddy.api.model.collection.ListResponse;
import com.paymybuddy.api.model.collection.PageResponse;
import com.paymybuddy.api.model.transaction.Transaction;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.api.model.user.UserBalance;
import com.paymybuddy.api.model.user.UserBalancesResponse;
import com.paymybuddy.api.request.auth.RegisterRequest;
import com.paymybuddy.api.request.transaction.CreateTransactionRequest;
import com.paymybuddy.api.request.transaction.WithdrawToBankRequest;
import com.paymybuddy.api.request.user.AddContactRequest;
import com.paymybuddy.auth.AuthService;
import com.paymybuddy.auth.provider.CredentialsAuthProvider;
import com.paymybuddy.auth.provider.TokenAuthProvider;
import com.paymybuddy.business.ContactService;
import com.paymybuddy.business.TransactionService;
import com.paymybuddy.business.UserService;
import com.paymybuddy.business.exception.ContactNotFoundException;
import com.paymybuddy.business.exception.EmailAlreadyRegisteredException;
import com.paymybuddy.business.exception.IllegalEmailException;
import com.paymybuddy.business.exception.IllegalNameException;
import com.paymybuddy.business.exception.IsHimselfException;
import com.paymybuddy.business.exception.NotEnoughFundsException;
import com.paymybuddy.business.exception.TooLongPasswordException;
import com.paymybuddy.business.exception.TooShortPasswordException;
import com.paymybuddy.server.mock.MockAuthGuard;
import com.paymybuddy.server.mock.MockTransactions;
import com.paymybuddy.server.mock.MockUsers;
import com.paymybuddy.server.mock.TestControllerConfig;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.paymybuddy.server.mock.MockMvcSnapshot.toMatchSnapshot;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(UserController.class)
@Import(TestControllerConfig.class)
@ExtendWith(SnapshotExtension.class)
class UserControllerTest {
    @MockBean
    private AuthService authService;
    @MockBean
    private CredentialsAuthProvider credentialsAuthProvider;
    @MockBean
    private TokenAuthProvider tokenAuthProvider;

    @MockBean
    private UserService userService;
    @MockBean
    private ContactService contactService;
    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register() throws Exception {
        Set<String> registered = new HashSet<>();
        when(userService.register(any(), any(), any(), any())).thenAnswer(m -> {
            String name = m.getArgument(0);
            String email = m.getArgument(1);
            String password = m.getArgument(2);
            Currency currency = m.getArgument(3);
            if (name.contains("--")) {
                throw new IllegalNameException();
            }
            if (!email.contains(".")) {
                throw new IllegalEmailException();
            }
            if (password.length() < 8) {
                throw new TooShortPasswordException(8);
            }
            if (password.length() >= 40) {
                throw new TooLongPasswordException(40);
            }
            if (!registered.add(email)) {
                throw new EmailAlreadyRegisteredException();
            }
            return MockUsers.newUser(1L);
        });
        toMatchSnapshot(
                mockMvc.perform(post("/user/register")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                                .name("name")
                                .email("email@domain.tld")
                                .password("password")
                                .defaultCurrency(Currency.USD)
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/register")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                                .name("name")
                                .email("email@domain.tld")
                                .password("password")
                                .defaultCurrency(Currency.USD)
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/register")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                                .name("bad--name")
                                .email("email@domain.tld")
                                .password("password")
                                .defaultCurrency(Currency.USD)
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/register")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                                .name("name")
                                .email("bad@email")
                                .password("password")
                                .defaultCurrency(Currency.USD)
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/register")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                                .name("name")
                                .email("email@domain.tld")
                                .password("short")
                                .defaultCurrency(Currency.USD)
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/register")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                                .name("name")
                                .email("email@domain.tld")
                                .password("longlonglonglonglonglonglonglonglonglong")
                                .defaultCurrency(Currency.USD)
                                .build())))
                        .andReturn(),

                mockMvc.perform(post("/user/register")
                        .header("x-auth-token", "anonymous"))
                        .andReturn(),
                mockMvc.perform(post("/user/register")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                                .email("email@domain.tld")
                                .password("password")
                                .defaultCurrency(Currency.USD)
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/register")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"x\":}"))
                        .andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void getBalances() throws Exception {
        User user = MockAuthGuard.get().getUser();
        when(userService.getUserBalances(user.getId())).thenReturn(UserBalancesResponse.builder()
                .defaultCurrency(user.getDefaultCurrency())
                .balance(UserBalance.builder()
                        .currency(Currency.USD)
                        .amount(new BigDecimal("123.45"))
                        .build())
                .balance(UserBalance.builder()
                        .currency(Currency.EUR)
                        .amount(new BigDecimal("543.21"))
                        .build())
                .build());
        toMatchSnapshot(
                mockMvc.perform(get("/user/balance")).andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void listContacts() throws Exception {
        User user = MockAuthGuard.get().getUser();
        when(contactService.listContacts(eq(user.getId()), any())).thenReturn(PageResponse.<User>builder()
                .page(1)
                .pageSize(2)
                .pageCount(20)
                .totalCount(39)
                .record(MockUsers.newContact(2L))
                .record(MockUsers.newContact(3L))
                .build());
        toMatchSnapshot(
                mockMvc.perform(get("/user/contact")).andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void addContact() throws Exception {
        User user = MockAuthGuard.get().getUser();
        User contact = MockUsers.newContact(2L);
        when(contactService.addContact(eq(user.getId()), any())).thenThrow(ContactNotFoundException.class);
        when(contactService.addContact(eq(user.getId()), eq(user.getEmail()))).thenThrow(IsHimselfException.class);
        when(contactService.addContact(eq(user.getId()), eq(contact.getEmail()))).thenReturn(contact);
        toMatchSnapshot(
                mockMvc.perform(post("/user/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddContactRequest.builder()
                                .email(contact.getEmail())
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddContactRequest.builder()
                                .email(user.getEmail())
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AddContactRequest.builder()
                                .email("unknown@domain.tld")
                                .build())))
                        .andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void getContact() throws Exception {
        User user = MockAuthGuard.get().getUser();
        User contact = MockUsers.newContact(2L);
        when(contactService.getContact(eq(user.getId()), eq(contact.getEmail()))).thenReturn(contact);
        when(contactService.getContact(eq(user.getId()), eq(contact.getName()))).thenReturn(contact);
        toMatchSnapshot(
                mockMvc.perform(get("/user/contact/{contactSelector}", contact.getEmail())).andReturn(),
                mockMvc.perform(get("/user/contact/{contactSelector}", contact.getName())).andReturn(),
                mockMvc.perform(get("/user/contact/{contactSelector}", "unknown@domain.tld")).andReturn(),
                mockMvc.perform(get("/user/contact/{contactSelector}", "unknown name")).andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void removeContact() throws Exception {
        User user = MockAuthGuard.get().getUser();
        User contact = MockUsers.newContact(2L);
        when(contactService.removeContact(eq(user.getId()), anyLong())).thenThrow(ContactNotFoundException.class);
        when(contactService.removeContact(eq(user.getId()), eq(contact.getId()))).thenReturn(contact);
        toMatchSnapshot(
                mockMvc.perform(delete("/user/contact/{contactId}", contact.getId())).andReturn(),
                mockMvc.perform(delete("/user/contact/{contactId}", 3L)).andReturn(),

                mockMvc.perform(delete("/user/contact/{contactId}", "xxx")).andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void autocompleteContact() throws Exception {
        User user = MockAuthGuard.get().getUser();
        when(contactService.searchContacts(eq(user.getId()), any(), anyInt())).thenReturn(ListResponse.of(Collections.emptyList()));
        when(contactService.searchContacts(eq(user.getId()), eq("abc"), anyInt())).thenReturn(ListResponse.of(Arrays.asList(MockUsers.newContact(2L), MockUsers.newContact(3L))));
        toMatchSnapshot(
                mockMvc.perform(get("/user/contact-autocomplete").queryParam("input", "abc")).andReturn(),
                mockMvc.perform(get("/user/contact-autocomplete").queryParam("input", "def")).andReturn(),

                mockMvc.perform(get("/user/contact-autocomplete")).andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void listTransactions() throws Exception {
        User user = MockAuthGuard.get().getUser();
        when(transactionService.listTransactions(eq(user.getId()), any())).thenReturn(CursorResponse.<Transaction>builder()
                .hasPrev(false)
                .prevCursor("a")
                .hasNext(false)
                .nextCursor("b")
                .record(MockTransactions.newTransaction(1L))
                .record(MockTransactions.newTransaction(2L))
                .build());
        toMatchSnapshot(
                mockMvc.perform(get("/user/transaction")).andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void createTransaction() throws Exception {
        User user = MockAuthGuard.get().getUser();
        BigDecimal userAmount = new BigDecimal("10.00");
        User recipient = MockUsers.newContact(2L);
        when(contactService.isContact(user.getId(), recipient.getId())).thenReturn(true);
        when(transactionService.createTransaction(eq(user.getId()), anyLong(), any(), any(), any(), any())).thenAnswer(m -> {
            Currency currency = m.getArgument(2);
            BigDecimal amount = m.getArgument(3);
            if (amount.compareTo(userAmount) > 0) {
                throw new NotEnoughFundsException(currency, amount.subtract(userAmount));
            }
            return MockTransactions.newTransaction(1L);
        });
        toMatchSnapshot(
                mockMvc.perform(post("/user/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTransactionRequest.builder()
                                .recipientId(recipient.getId())
                                .currency(Currency.USD)
                                .amount(new BigDecimal("5.00"))
                                .description("Ok")
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTransactionRequest.builder()
                                .recipientId(recipient.getId())
                                .currency(Currency.USD)
                                .amount(new BigDecimal("15.00"))
                                .description("Too many")
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CreateTransactionRequest.builder()
                                .recipientId(3L)
                                .currency(Currency.USD)
                                .amount(new BigDecimal("5.00"))
                                .description("Not contact")
                                .build())))
                        .andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void withdrawToBankAccount() throws Exception {
        User user = MockAuthGuard.get().getUser();
        BigDecimal userAmount = new BigDecimal("10.00");
        when(transactionService.withdrawToBank(eq(user.getId()), any(), any(), any())).thenAnswer(m -> {
            Currency currency = m.getArgument(1);
            BigDecimal amount = m.getArgument(2);
            if (amount.compareTo(userAmount) > 0) {
                throw new NotEnoughFundsException(currency, amount.subtract(userAmount));
            }
            return true;
        });
        toMatchSnapshot(
                mockMvc.perform(post("/user/withdraw-to-bank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(WithdrawToBankRequest.builder()
                                .currency(Currency.USD)
                                .amount(new BigDecimal("5.00"))
                                .iban("NL91ABNA0417164300")
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/user/withdraw-to-bank")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(WithdrawToBankRequest.builder()
                                .currency(Currency.USD)
                                .amount(new BigDecimal("15.00"))
                                .iban("NL91ABNA0417164300")
                                .build())))
                        .andReturn()
        );
    }
}