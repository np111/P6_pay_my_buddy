package com.paymybuddy.server.http.controller;

import com.paymybuddy.api.model.ApiError;
import com.paymybuddy.api.model.ApiError.ErrorCode;
import com.paymybuddy.api.model.ApiError.ErrorType;
import com.paymybuddy.api.model.collection.CursorResponse;
import com.paymybuddy.api.model.collection.ListResponse;
import com.paymybuddy.api.model.collection.PageResponse;
import com.paymybuddy.api.model.transaction.Transaction;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.api.model.user.UserBalancesResponse;
import com.paymybuddy.api.request.auth.RegisterRequest;
import com.paymybuddy.api.request.transaction.CreateTransactionRequest;
import com.paymybuddy.api.request.transaction.WithdrawToBankRequest;
import com.paymybuddy.api.request.user.AddContactRequest;
import com.paymybuddy.api.util.jackson.AmountSerializer;
import com.paymybuddy.auth.AuthGuard;
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
import com.paymybuddy.business.pageable.CursorRequestParser;
import com.paymybuddy.business.pageable.PageRequestParser;
import com.paymybuddy.business.util.DateUtil;
import com.paymybuddy.server.http.util.JsonRequestMapping;
import com.paymybuddy.server.springdoc.error.ApiErrorResponse;
import com.paymybuddy.server.springdoc.param.ApiCursorRequestParameter;
import com.paymybuddy.server.springdoc.param.ApiPageRequestParameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.constraints.Size;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import static com.paymybuddy.server.http.controller.ExceptionController.errorToResponse;

@Tag(name = "user", description = "Users operations")
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

    @Operation(
            summary = "Register a new user."
    )
    @ApiErrorResponse(method = "handleIllegalNameException")
    @ApiErrorResponse(method = "handleIllegalEmailException")
    @ApiErrorResponse(method = "handleTooShortPasswordException")
    @ApiErrorResponse(method = "handleTooLongPasswordException")
    @ApiErrorResponse(method = "handleEmailAlreadyRegisteredException")
    @PreAuthorize("isAnonymous()")
    @JsonRequestMapping(method = RequestMethod.POST, value = "/register")
    public ResponseEntity<Void> register(
            @RequestBody @Validated RegisterRequest body
    ) {
        userService.register(body.getName(), body.getEmail(), body.getPassword(), body.getDefaultCurrency());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Returns the user's balances.",
            description = "The default currency balance is always included (first)."
                    + " Others are only included when they are non-zero."
    )
    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/balance")
    public UserBalancesResponse getBalances(
            @AuthenticationPrincipal AuthGuard auth
    ) {
        return userService.getUserBalances(auth.getUserId());
    }

    @Operation(
            summary = "Returns the user's contact list."
    )
    @ApiPageRequestParameter
    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/contact")
    public PageResponse<User> listContacts(
            @AuthenticationPrincipal AuthGuard auth,
            WebRequest webRequest
    ) {
        return contactService.listContacts(auth.getUserId(), CONTACT_REQUEST_PARSER.of(webRequest::getParameter));
    }

    @Operation(
            summary = "Adds a contact to the user."
    )
    @ApiErrorResponse(method = "handleContactNotFoundException")
    @ApiErrorResponse(method = "handleIsHimselfException")
    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.POST, value = "/contact")
    public User addContact(
            @AuthenticationPrincipal AuthGuard auth,
            @RequestBody @Validated AddContactRequest body
    ) {
        return contactService.addContact(auth.getUserId(), body.getEmail());
    }

    @Operation(
            summary = "Returns a contact of the user."
    )
    @ApiErrorResponse(method = "handleContactNotFoundException")
    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/contact/{contactSelector}")
    public User getContact(
            @AuthenticationPrincipal AuthGuard auth,
            @Parameter(description = "Email or name of the contact.")
            @PathVariable("contactSelector") String contactSelector
    ) {
        User contact = contactService.getContact(auth.getUserId(), contactSelector);
        if (contact == null) {
            throw new ContactNotFoundException();
        }
        return contact;
    }

    @Operation(
            summary = "Removes a user contact."
    )
    @ApiErrorResponse(method = "handleContactNotFoundException")
    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.DELETE, value = "/contact/{contactId}")
    public User removeContact(
            @AuthenticationPrincipal AuthGuard auth,
            @Parameter(description = "ID of the contact.")
            @PathVariable("contactId") Long contactId
    ) {
        return contactService.removeContact(auth.getUserId(), contactId);
    }

    @Operation(
            summary = "Autocomplete the user's contacts."
    )
    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/contact-autocomplete")
    public ListResponse<User> autocompleteContact(
            @AuthenticationPrincipal AuthGuard auth,
            @Parameter(description = "Partial term to search/autocomplete.")
            @RequestParam("input") @Size(max = 255) String input
    ) {
        return contactService.searchContacts(auth.getUserId(), input, 10);
    }

    @Operation(
            summary = "Returns the user's transaction list.",
            description = "Lists the transactions of which the user is the sender or the recipient."
    )
    @ApiCursorRequestParameter
    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/transaction")
    public CursorResponse<Transaction> listTransactions(
            @AuthenticationPrincipal AuthGuard auth,
            WebRequest webRequest
    ) {
        return transactionService.listTransactions(auth.getUserId(), TRANSACTION_REQUEST_PARSER.of(webRequest::getParameter));
    }

    @Operation(
            summary = "Create a new transaction."
    )
    @ApiErrorResponse(method = "handleContactNotFoundException")
    @ApiErrorResponse(method = "handleNotEnoughFundsException")
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

    @Operation(
            summary = "Withdraw balance to a bank account."
    )
    @ApiErrorResponse(method = "handleNotEnoughFundsException")
    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.POST, value = "/withdraw-to-bank")
    public ResponseEntity<Void> withdrawToBankAccount(
            @AuthenticationPrincipal AuthGuard auth,
            @RequestBody @Validated WithdrawToBankRequest body
    ) {
        transactionService.withdrawToBank(auth.getUserId(), body.getCurrency(), body.getAmount(), body.getIban());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleEmailAlreadyRegisteredException() {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.SERVICE)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_EMAIL)
                .message("Email already registered")
                .metadata("alreadyExists", true)
                .build());
    }

    @ExceptionHandler(IllegalEmailException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleIllegalEmailException() {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.SERVICE)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_EMAIL)
                .message("Illegal email")
                .build());
    }

    @ExceptionHandler(IllegalNameException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleIllegalNameException() {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.SERVICE)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_NAME)
                .message("Illegal name")
                .build());
    }

    @ExceptionHandler(TooShortPasswordException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleTooShortPasswordException(TooShortPasswordException ex) {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.SERVICE)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_PASSWORD)
                .message("Password is too short")
                .metadata("minLength", ex.getLength())
                .build());
    }

    @ExceptionHandler(TooLongPasswordException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleTooLongPasswordException(TooLongPasswordException ex) {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.SERVICE)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_PASSWORD)
                .message("Password is too long")
                .metadata("maxLength", ex.getLength())
                .build());
    }

    @ExceptionHandler(ContactNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleContactNotFoundException() {
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
    public ResponseEntity<ApiError> handleIsHimselfException() {
        return errorToResponse(ApiError.builder()
                .type(ErrorType.SERVICE)
                .status(HttpStatus.PRECONDITION_FAILED.value())
                .code(ErrorCode.CANNOT_BE_HIMSELF)
                .message("You cannot perform this action on yourself")
                .build());
    }
}
