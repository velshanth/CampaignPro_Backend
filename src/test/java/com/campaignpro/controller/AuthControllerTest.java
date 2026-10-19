package com.campaignpro.controller;

import com.campaignpro.dto.AuthResponse;
import com.campaignpro.dto.LoginRequest;
import com.campaignpro.dto.RegisterRequest;
import com.campaignpro.model.User;
import com.campaignpro.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserDetails userDetails;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
        testUser.setRole("USER");
        testUser.setActive(true);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFullName("New User");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("jwt-token");

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        var result = authController.register(request);

        assertNotNull(result);
        assertEquals(CREATED, result.getStatusCode());
        assertEquals("jwt-token", result.getBody().getToken());
    }

    @Test
    void testRegisterUserExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        when(authService.register(any(RegisterRequest.class))).thenThrow(new RuntimeException("User already exists"));

        assertThrows(RuntimeException.class, () -> {
            authController.register(request);
        });
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("jwt-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        var result = authController.login(request);

        assertNotNull(result);
        assertEquals(OK, result.getStatusCode());
        assertEquals("jwt-token", result.getBody().getToken());
    }

    @Test
    void testLoginUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(authService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            authController.login(request);
        });
    }
}
