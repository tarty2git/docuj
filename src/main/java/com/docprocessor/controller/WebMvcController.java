package com.docprocessor.controller;

import com.docprocessor.model.ProcessedDocument;
import com.docprocessor.service.DocumentParsingService;
import com.docprocessor.service.SystemConfigurationService;
import com.docprocessor.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebMvcController {

    private final DocumentParsingService documentService;
    private final SystemConfigurationService configService;
    private final UserService userService;

    public WebMvcController(DocumentParsingService documentService, SystemConfigurationService configService, UserService userService) {
        this.documentService = documentService;
        this.configService = configService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("title", configService.getAppTitle());
        model.addAttribute("description", configService.getAppDescription());
        model.addAttribute("version", configService.getAppVersion());
        model.addAttribute("documentCode", configService.getAppDocumentCode());
        model.addAttribute("ldapEnabled", configService.isLdapEnabled());

        List<ProcessedDocument> docs = documentService.getAllProcessedDocuments();
        model.addAttribute("documents", docs);

        if (auth != null) {
            model.addAttribute("username", auth.getName());
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);

            // Check if current user requires a password reset
            userService.findByUsername(auth.getName()).ifPresent(u -> {
                model.addAttribute("passwordResetRequired", u.isPasswordResetRequired());
            });
        }
        return "dashboard";
    }

    @GetMapping("/login")
    public String login(Model model, @RequestParam(required = false) String error, @RequestParam(required = false) String logout) {
        model.addAttribute("title", configService.getAppTitle());
        model.addAttribute("version", configService.getAppVersion());
        model.addAttribute("documentCode", configService.getAppDocumentCode());
        model.addAttribute("ldapEnabled", configService.isLdapEnabled());
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid Username/Password or Account Status Restricted.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", configService.getAppTitle());
        model.addAttribute("version", configService.getAppVersion());
        model.addAttribute("documentCode", configService.getAppDocumentCode());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegistration(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam(defaultValue = "ROLE_USER") String role,
            Model model) {

        model.addAttribute("title", configService.getAppTitle());
        model.addAttribute("version", configService.getAppVersion());
        model.addAttribute("documentCode", configService.getAppDocumentCode());

        try {
            userService.registerUser(username, password, email, role);
            model.addAttribute("successMessage", "Account created successfully! You can now log in.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/admin")
    public String adminPanel(Model model, Authentication auth) {
        model.addAttribute("title", configService.getAppTitle());
        model.addAttribute("description", configService.getAppDescription());
        model.addAttribute("version", configService.getAppVersion());
        model.addAttribute("documentCode", configService.getAppDocumentCode());
        model.addAttribute("ldapEnabled", configService.isLdapEnabled());
        model.addAttribute("users", userService.getAllUsers());

        if (auth != null) {
            model.addAttribute("username", auth.getName());
        }
        return "admin";
    }

    @PostMapping("/password-reset-force")
    public String forcePasswordReset(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            Authentication auth,
            Model model) {

        try {
            userService.changePassword(auth.getName(), currentPassword, newPassword);
            return "redirect:/?passwordResetSuccess=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/?passwordResetError=" + e.getMessage();
        }
    }
}
