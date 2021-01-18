package com.paymybuddy.server.http.controller;

import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;

import static com.paymybuddy.server.mock.MockMvcSnapshot.toMatchSnapshot;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(ExceptionController.class)
@Import(TestControllerConfig.class)
@ExtendWith(SnapshotExtension.class)
class ExceptionControllerTest {
    @MockBean
    private CredentialsAuthProvider credentialsAuthProvider;
    @MockBean
    private TokenAuthProvider tokenAuthProvider;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @MockAuthGuard.WithAuthToken
    void handleNoHandlerFoundException() throws Exception {
        toMatchSnapshot(
                mockMvc.perform(get("/no-handler-found")).andReturn()
        );
    }
}