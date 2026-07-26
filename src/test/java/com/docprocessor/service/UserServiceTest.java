package com.docprocessor.service;

import com.docprocessor.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testRegisterUser_Success() {
        User user = userService.registerUser("newuser", "SecurePass123!", "user@test.com", "ROLE_USER");
        assertNotNull(user.getId());
        assertEquals("newuser", user.getUsername());
        assertFalse(user.isLocked());
        assertFalse(user.isPasswordResetRequired());
    }

    @Test
    public void testRegisterUser_WeakPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("weak", "123", "weak@test.com", "ROLE_USER");
        });
    }

    @Test
    public void testUpdateUserStatus() {
        User user = userService.registerUser("statususer", "SecurePass123!", "status@test.com", "ROLE_USER");

        // Lock user
        userService.updateUserStatus(user.getId(), true, null, null, null);
        assertTrue(user.isLocked());

        // Unlock user
        userService.updateUserStatus(user.getId(), null, true, null, null);
        assertFalse(user.isLocked());

        // Password Reset Required
        userService.updateUserStatus(user.getId(), null, null, true, null);
        assertTrue(user.isPasswordResetRequired());
    }
}
