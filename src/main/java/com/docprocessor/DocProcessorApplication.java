package com.docprocessor;

import com.docprocessor.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DocProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocProcessorApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(UserService userService) {
        return args -> {
            // Seed a default admin and default user on startup if they don't already exist
            if (userService.findByUsername("admin").isEmpty()) {
                userService.registerUser("admin", "AdminPass123!", "admin@docprocessor.com", "ROLE_ADMIN");
            }
            if (userService.findByUsername("user").isEmpty()) {
                userService.registerUser("user", "UserPass123!", "user@docprocessor.com", "ROLE_USER");
            }
        };
    }
}
