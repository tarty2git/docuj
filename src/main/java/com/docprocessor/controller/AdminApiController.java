package com.docprocessor.controller;

import com.docprocessor.model.User;
import com.docprocessor.service.UserService;
import com.docprocessor.service.SystemConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Operations Controller", description = "Admin APIs for user accounts, LDAP setup, and application configs")
public class AdminApiController {

    private final UserService userService;
    private final SystemConfigurationService configService;

    public AdminApiController(UserService userService, SystemConfigurationService configService) {
        this.userService = userService;
        this.configService = configService;
    }

    @GetMapping("/users")
    @Operation(summary = "Get list of all users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users/{userId}/status")
    @Operation(summary = "Update user status (lock, unlock, reset password flag, or credentials expiry)")
    public ResponseEntity<Map<String, String>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean lock,
            @RequestParam(required = false) Boolean unlock,
            @RequestParam(required = false) Boolean resetPassword,
            @RequestParam(required = false) Boolean expireCredentials) {

        userService.updateUserStatus(userId, lock, unlock, resetPassword, expireCredentials);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "User status updated successfully");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/users/{userId}/reset-password")
    @Operation(summary = "Reset user's password")
    public ResponseEntity<Map<String, String>> resetUserPassword(
            @PathVariable Long userId,
            @RequestParam String newPassword) {
        userService.resetPassword(userId, newPassword);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "Password reset successful.");
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user by ID")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "User deleted successfully");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/config")
    @Operation(summary = "Update dynamic application details")
    public ResponseEntity<Map<String, String>> updateSystemConfig(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String version,
            @RequestParam String documentCode,
            @RequestParam boolean ldapEnabled) {

        configService.setAppTitle(title);
        configService.setAppDescription(description);
        configService.setAppVersion(version);
        configService.setAppDocumentCode(documentCode);
        configService.setLdapEnabled(ldapEnabled);

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "System parameters updated successfully");
        return ResponseEntity.ok(resp);
    }
}
