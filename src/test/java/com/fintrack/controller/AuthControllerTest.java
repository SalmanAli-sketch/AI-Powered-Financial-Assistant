package com.fintrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.dto.JwtResponse;
import com.fintrack.dto.LoginRequest;
import com.fintrack.dto.RegisterRequest;
import com.fintrack.service.AuthService;
import com.fintrack.security.JwtTokenProvider;
import com.fintrack.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("User One");
        request.setEmail("user1@example.com");
        request.setPassword("password123");

        JwtResponse jwtResponse = new JwtResponse("token123", "user1@example.com", "User One", "USER");
        when(authService.register(any(RegisterRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.email").value("user1@example.com"))
                .andExpect(jsonPath("$.name").value("User One"));
    }

    @Test
    public void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user1@example.com");
        request.setPassword("password123");

        JwtResponse jwtResponse = new JwtResponse("token123", "user1@example.com", "User One", "USER");
        when(authService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.email").value("user1@example.com"));
    }
}
