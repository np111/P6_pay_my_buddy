package com.paymybuddy.server.http.controller;

import com.paymybuddy.api.model.ApiError;
import com.paymybuddy.api.model.ApiError.ErrorCode;
import com.paymybuddy.api.model.ApiError.ErrorType;
import com.paymybuddy.api.model.collection.CursorResponse;
import com.paymybuddy.api.model.collection.PageResponse;
import com.paymybuddy.api.model.transaction.Transaction;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.api.model.user.UserBalancesResponse;
import com.paymybuddy.api.request.transaction.CreateTransactionRequest;
import com.paymybuddy.api.request.user.AddContactRequest;
import com.paymybuddy.api.util.jackson.AmountSerializer;
import com.paymybuddy.auth.AuthGuard;
import com.paymybuddy.business.ContactService;
import com.paymybuddy.business.TransactionService;
import com.paymybuddy.business.UserService;
import com.paymybuddy.business.exception.ContactNotFoundException;
import com.paymybuddy.business.exception.IsHimselfException;
import com.paymybuddy.business.exception.NotEnoughFundsException;
import com.paymybuddy.business.pageable.CursorRequestParser;
import com.paymybuddy.business.pageable.PageRequestParser;
import com.paymybuddy.business.util.DateUtil;
import com.paymybuddy.server.http.util.JsonRequestMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import static com.paymybuddy.server.http.controller.ExceptionController.errorToResponse;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    private static final PageRequestParser CONTACT_REQUEST_PARSER = PageRequestParser.builder()
            .minPageSize(1) // FIXME: small value for tests/examples only
            .maxPageSize(100)
            .defaultPageSize(20)
            .sortableProperty("email")
            .sortableProperty("name")
            .defaultSort("name")
            .build();
    private static final CursorRequestParser TRANSACTION_REQUEST_PARSER = CursorRequestParser.builder()
            .minPageSize(1) // FIXME: small value for tests/examples only
            .maxPageSize(100)
            .defaultPageSize(20)
            .sortableProperty("id")
            .sortableProperty("amount")
            .defaultSort("id")
            .build();

    private final UserService userService;
    private final ContactService contactService;
    private final TransactionService transactionService;

    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/balance")
    public UserBalancesResponse getBalances(
            @AuthenticationPrincipal AuthGuard auth
    ) {
        return userService.getUserBalances(auth.getUserId());
    }

    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/contact")
    public PageResponse<User> listContacts(
            @AuthenticationPrincipal AuthGuard auth,
            WebRequest webRequest
    ) {
        return contactService.listContacts(auth.getUserId(), CONTACT_REQUEST_PARSER.of(webRequest::getParameter));
    }

    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.POST, value = "/contact")
    public User addContact(
            @AuthenticationPrincipal AuthGuard auth,
            @RequestBody @Validated AddContactRequest body
    ) {
        return contactService.addContact(auth.getUserId(), body.getEmail());
    }

    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.DELETE, value = "/contact/{contactId}")
    public User removeContact(
            @AuthenticationPrincipal AuthGuard auth,
            @PathVariable("contactId") Long contactId
    ) {
        return contactService.removeContact(auth.getUserId(), contactId);
    }

    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/transaction")
    public CursorResponse<Transaction> listTransactions(
            @AuthenticationPrincipal AuthGuard auth,
            WebRequest webRequest
    ) {
        return transactionService.listTransactions(auth.getUserId(), TRANSACTION_REQUEST_PARSER.of(webRequest::getParameter));
    }

    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.POST, value = "/transaction")
    public Transaction createTransaction(
            @AuthenticationPrincipal AuthGuard auth,
            @RequestBody @Validated CreateTransactionRequest body
    ) {
        long userId = auth.getUserId();
        long recipientId = body.getRecipientId();
        if (!contactService.isContact(userId, recipientId)) {
            throw new ContactNotFoundException();
        }
        return transactionService.createTransaction(
                userId, recipientId, body.getCurrency(), body.getAmount(), body.getDescription(), DateUtil.now());
    }

    @ExceptionHandler(ContactNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleContactNotFoundException(ContactNotFoundException ex) {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.SERVICE)
                .status(HttpStatus.NOT_FOUND.value())
                .code(ErrorCode.CONTACT_NOT_FOUND)
                .message("Contact does not exists")
                .build());
    }

    @ExceptionHandler(NotEnoughFundsException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleNotEnoughFundsException(NotEnoughFundsException ex) {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.SERVICE)
                .status(HttpStatus.PRECONDITION_FAILED.value())
                .code(ErrorCode.NOT_ENOUGH_FUNDS)
                .message("You don't have enough funds")
                .metadata("currency", ex.getCurrency())
                .metadata("missingAmount", AmountSerializer.toString(ex.getMissingAmount()))
                .build());
    }

    @ExceptionHandler(IsHimselfException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleIsHimselfException(IsHimselfException ex) {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.SERVICE)
                .status(HttpStatus.PRECONDITION_FAILED.value())
                .code(ErrorCode.CANNOT_BE_HIMSELF)
                .message("You cannot perform this action on yourself")
                .build());
    }
}
