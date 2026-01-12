package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.auth.usecase.*;
import com.app.easypharma_backend.infrastructure.security.JwtService;
import com.app.easypharma_backend.domain.auth.service.AuditService;
import com.app.easypharma_backend.infrastructure.security.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(com.app.easypharma_backend.infrastructure.config.WebSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private RegisterUseCase registerUseCase;

        @MockBean
        private LoginUseCase loginUseCase;

        @MockBean
        private RefreshTokenUseCase refreshTokenUseCase;

        @MockBean
        private LogoutUseCase logoutUseCase;

        @MockBean
        private ForgotPasswordUseCase forgotPasswordUseCase;

        @MockBean
        private ResetPasswordUseCase resetPasswordUseCase;

        @MockBean
        private GetUserProfileUseCase getUserProfileUseCase;

        @MockBean
        private JwtService jwtService;

        @MockBean
        private AuditService auditService;

        @MockBean
        private RateLimitingService rateLimitingService;

        @MockBean
        private UserDetailsService userDetailsService;

        @MockBean
        private com.app.easypharma_backend.infrastructure.security.JwtFilter jwtFilter;

        @Test
        void register_shouldReturnOk_andCallRegisterUseCase() throws Exception {
                String json = objectMapper.writeValueAsString(
                                new java.util.HashMap<String, Object>() {
                                        {
                                                put("email", "user@example.com");
                                                put("password", "P@ssw0rdA");
                                                put("firstName", "John");
                                                put("lastName", "Doe");
                                                put("phone", "600000000");
                                                put("role", "PATIENT");
                                        }
                                });

                when(registerUseCase.execute(any())).thenReturn(null);

                mockMvc.perform(post("/api/v1/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk());

                verify(registerUseCase, times(1)).execute(any());
        }

        @Test
        void login_shouldReturnOk_andCallLoginUseCase() throws Exception {
                String json = objectMapper.writeValueAsString(
                                new java.util.HashMap<String, String>() {
                                        {
                                                put("email", "user@example.com");
                                                put("password", "P@ssw0rd");
                                        }
                                });

                when(loginUseCase.execute(any())).thenReturn(null);

                mockMvc.perform(post("/api/v1/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk());

                verify(loginUseCase, times(1)).execute(any());
        }

        @Test
        void refreshToken_shouldReturnOk_andCallRefreshTokenUseCase() throws Exception {
                String json = objectMapper.writeValueAsString(
                                new java.util.HashMap<String, String>() {
                                        {
                                                put("refreshToken", "refresh-token-value");
                                        }
                                });

                when(refreshTokenUseCase.execute(any())).thenReturn(null);

                mockMvc.perform(post("/api/v1/auth/refresh-token")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk());

                verify(refreshTokenUseCase, times(1)).execute(any());
        }

        @Test
        void logout_shouldReturnOk_andCallLogoutUseCase() throws Exception {
                String json = objectMapper.writeValueAsString(
                                new java.util.HashMap<String, String>() {
                                        {
                                                put("refreshToken", "refresh-token-value");
                                        }
                                });

                doNothing().when(logoutUseCase).execute(anyString());

                mockMvc.perform(post("/api/v1/auth/logout")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk());

                verify(logoutUseCase, times(1)).execute(eq("refresh-token-value"));
        }

        @Test
        void forgotPassword_shouldReturnOk_andCallForgotPasswordUseCase() throws Exception {
                String json = objectMapper.writeValueAsString(
                                new java.util.HashMap<String, String>() {
                                        {
                                                put("email", "user@example.com");
                                        }
                                });

                when(rateLimitingService.tryConsume(anyString())).thenReturn(true);
                doNothing().when(forgotPasswordUseCase).execute(anyString(), anyString(), anyString());

                mockMvc.perform(post("/api/v1/auth/forgot-password")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk());

                verify(forgotPasswordUseCase, times(1)).execute(eq("user@example.com"), any(), any());
        }

        @Test
        void resetPassword_shouldReturnOk_andCallResetPasswordUseCase() throws Exception {
                String json = objectMapper.writeValueAsString(
                                new java.util.HashMap<String, String>() {
                                        {
                                                put("token", "reset-token");
                                                put("newPassword", "NewP@ss1");
                                        }
                                });

                doNothing().when(resetPasswordUseCase).execute(anyString(), anyString());

                mockMvc.perform(post("/api/v1/auth/reset-password")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk());

                verify(resetPasswordUseCase, times(1)).execute(eq("reset-token"), eq("NewP@ss1"));
        }

        @Test
        void getCurrentUser_shouldReturnOk_andCallGetUserProfileUseCase() throws Exception {
                String bearer = "Bearer my-jwt-token";
                when(jwtService.extractEmail("my-jwt-token")).thenReturn("user@example.com");
                when(getUserProfileUseCase.execute(anyString())).thenReturn(null);

                mockMvc.perform(get("/api/v1/auth/me")
                                .header("Authorization", bearer))
                                .andExpect(status().isOk());

                verify(jwtService, times(1)).extractEmail("my-jwt-token");
                verify(getUserProfileUseCase, times(1)).execute(eq("user@example.com"));
        }
}
