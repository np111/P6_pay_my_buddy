package com.paymybuddy.server.http.controller;

import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.api.request.auth.LoginRequest;
import com.paymybuddy.auth.AuthService;
import com.paymybuddy.auth.AuthToken;
import com.paymybuddy.auth.provider.CredentialsAuthProvider;
import com.paymybuddy.auth.provider.TokenAuthProvider;
import com.paymybuddy.server.mock.MockAuthGuard;
import com.paymybuddy.server.mock.TestControllerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static com.paymybuddy.server.mock.MockMvcSnapshot.toMatchSnapshot;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(AuthController.class)
@Import(TestControllerConfig.class)
@ExtendWith(SnapshotExtension.class)
class AuthControllerTest {
    @MockBean
    private AuthService authService;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private CredentialsAuthProvider credentialsAuthProvider;
    @MockBean
    private TokenAuthProvider tokenAuthProvider;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login() throws Exception {
        when(authService.login(any(), any())).thenAnswer(m -> {
            String email = m.getArgument(0);
            String password = m.getArgument(1);
            if ("email@domain.tld".equals(email) && "password".equals(password)) {
                return AuthToken.authenticated(MockAuthGuard.create(), "SECRET");
            }
            throw new BadCredentialsException("Bad credentials");
        });
        toMatchSnapshot(
                mockMvc.perform(post("/auth/login")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email("email@domain.tld")
                                .password("password")
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/auth/login")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email("bad_email@domain.tld")
                                .password("password")
                                .build())))
                        .andReturn(),
                mockMvc.perform(post("/auth/login")
                        .header("x-auth-token", "anonymous")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email("email@domain.tld")
                                .password("bad password")
                                .build())))
                        .andReturn()
        );
    }

    @Test
    void rememberNotLogged() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
        toMatchSnapshot(
                mockMvc.perform(get("/auth/remember")
                        .header("x-auth-token", "expired-token"))
                        .andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void rememberLogged() throws Exception {
        toMatchSnapshot(
                mockMvc.perform(get("/auth/remember")).andReturn()
        );
    }

    @Test
    @MockAuthGuard.WithAuthToken
    void logout() throws Exception {
        String token = ((AuthToken) SecurityContextHolder.getContext().getAuthentication()).getCredentials();
        toMatchSnapshot(
                mockMvc.perform(post("/auth/logout")).andReturn()
        );
        verify(authService, times(1)).destroyAuthToken(token);
    }
}