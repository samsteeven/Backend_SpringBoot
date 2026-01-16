package com.app.easypharma_backend.presentation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin@test.com", roles = "SUPER_ADMIN")
    void shouldAllowAccessToGetAllUsers_WhenUserIsSuperAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "PATIENT")
    void shouldDenyAccessToGetAllUsers_WhenUserIsNotSuperAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
