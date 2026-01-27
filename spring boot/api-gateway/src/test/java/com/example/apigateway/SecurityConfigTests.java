package com.example.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SecurityConfigTests {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    @Test
    void securityConfigBeanCreated() {
        assertNotNull(securityConfig);
    }

    @Test
    void securityFilterChainIsNotNull() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        
        // Mock the builder pattern
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2Login(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(SecurityFilterChain.class));
        
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);
        
        assertNotNull(chain);
    }

    @Test
    void oauth2LoginConfigured() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2Login(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(SecurityFilterChain.class));
        
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);
        
        assertNotNull(chain);
        verify(httpSecurity, times(1)).oauth2Login(any());
    }

    @Test
    void authorizeHttpRequestsConfigured() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2Login(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(SecurityFilterChain.class));
        
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);
        
        assertNotNull(chain);
        verify(httpSecurity, times(1)).authorizeHttpRequests(any());
    }

    @Test
    void securityFilterChainBuildsSuccessfully() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        SecurityFilterChain mockChain = mock(SecurityFilterChain.class);
        
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2Login(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mockChain);
        
        SecurityFilterChain result = securityConfig.securityFilterChain(httpSecurity);
        
        assertEquals(mockChain, result);
    }

    @Test
    void securityConfigProtectsAllRequests() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.oauth2Login(any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(mock(SecurityFilterChain.class));
        
        // Verify that authorize is called
        securityConfig.securityFilterChain(httpSecurity);
        
        verify(httpSecurity).authorizeHttpRequests(any());
    }
}
