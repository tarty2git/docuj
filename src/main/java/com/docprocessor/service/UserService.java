package com.docprocessor.service;

import com.docprocessor.model.User;
import com.docprocessor.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Minimum 8 chars, at least one digit, one uppercase letter, one special character.
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$"
    );

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User registerUser(String username, String password, String email, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }
        validatePassword(password);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(role);
        user.setCredentialsExpiryDate(LocalDateTime.now().plusDays(90));

        return userRepository.save(user);
    }

    @Transactional
    public void updateUserStatus(Long userId, Boolean lock, Boolean unlock, Boolean resetPassword, Boolean expireCredentials) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (lock != null && lock) {
            user.setLocked(true);
        }
        if (unlock != null && unlock) {
            user.setLocked(false);
        }
        if (resetPassword != null && resetPassword) {
            user.setPasswordResetRequired(true);
        }
        if (expireCredentials != null && expireCredentials) {
            user.setCredentialsExpiryDate(LocalDateTime.now().minusDays(1)); // expired yesterday
        }
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetRequired(false);
        user.setCredentialsExpiryDate(LocalDateTime.now().plusDays(90)); // renew expiration
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password.");
        }
        validatePassword(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetRequired(false);
        user.setCredentialsExpiryDate(LocalDateTime.now().plusDays(90));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public void validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters long, contain at least one uppercase letter, one digit, and one special character (@#$%^&+=!).");
        }
    }
}
