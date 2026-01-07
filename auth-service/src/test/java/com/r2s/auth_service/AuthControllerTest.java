package com.r2s.auth_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.auth_service.controller.AuthController;
import com.r2s.auth_service.service.AuthService;
import com.r2s.core_service.dto.request.LoginRequest;
import com.r2s.core_service.dto.request.RegisterRequest;
import com.r2s.core_service.dto.response.AccountResponse;
import com.r2s.core_service.dto.response.TokenResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void register_success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("username")
                .password("123")
                .confirmPassword("123")
                .build();

        AccountResponse response = AccountResponse.builder()
                .username("username")
                .build();

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.username").value("username"));
    }

    @Test
    void login_success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("username")
                .password("123")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .token("token")
                .build();

        when(authService.login(any())).thenReturn(tokenResponse);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").value("token"));
    }

}
