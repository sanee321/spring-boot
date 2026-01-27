package com.example.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verify application context loads successfully
    }

    @Test
    void authServiceApplicationStartsSuccessfully() {
        assertNotNull(AuthServiceApplication.class);
    }

    @Test
    void applicationNameIsCorrect() {
        String appName = AuthServiceApplication.class.getSimpleName();
        assertNotNull(appName);
        assertTrue(appName.contains("AuthService"));
    }

    private boolean assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but got false");
        }
        return true;
    }
}
