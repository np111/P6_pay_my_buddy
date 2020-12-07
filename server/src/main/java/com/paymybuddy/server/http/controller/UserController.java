package com.paymybuddy.server.http.controller;

import com.paymybuddy.api.model.collection.ListResponse;
import com.paymybuddy.api.model.collection.PageableResponse;
import com.paymybuddy.api.model.user.User;
import com.paymybuddy.api.model.user.UserBalance;
import com.paymybuddy.api.request.user.AddContactRequest;
import com.paymybuddy.server.http.auth.AuthGuard;
import com.paymybuddy.server.jpa.util.PageableFetcher;
import com.paymybuddy.server.service.ContactService;
import com.paymybuddy.server.service.UserService;
import com.paymybuddy.server.util.spring.JsonRequestMapping;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    private final UserService userService;
    private final ContactService contactService;

    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/balance")
    public ListResponse<UserBalance> getBalances(
            @AuthenticationPrincipal AuthGuard auth
    ) {
        List<UserBalance> balances = userService.getUserBalances(auth.getUserId());
        // TODO: handle null
        return ListResponse.of(balances);
    }

    @PreAuthorize("isAuthenticated()")
    @JsonRequestMapping(method = RequestMethod.GET, value = "/contact")
    public PageableResponse<User> listContacts(
            @AuthenticationPrincipal AuthGuard auth,
            WebRequest webRequest
    ) {
        return contactService.listContacts(auth.getUserId(), new PageableFetcher.WebParams(webRequest));
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

    // TODO: Handle ContactNotFoundException
}
