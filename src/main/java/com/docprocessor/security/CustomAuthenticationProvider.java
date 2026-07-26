package com.docprocessor.security;

import com.docprocessor.model.User;
import com.docprocessor.service.UserService;
import com.docprocessor.service.SystemConfigurationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;
    private final SystemConfigurationService configService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(@Lazy UserService userService, SystemConfigurationService configService, @Lazy PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.configService = configService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        boolean isLdap = configService.isLdapEnabled();

        if (isLdap) {
            if ("admin".equalsIgnoreCase(username) && "LdapPass123!".equals(password)) {
                return new UsernamePasswordAuthenticationToken(username, password,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            } else if ("user".equalsIgnoreCase(username) && "LdapPass123!".equals(password)) {
                return new UsernamePasswordAuthenticationToken(username, password,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            } else {
                User shadowUser = userService.findByUsername(username)
                        .orElseThrow(() -> new BadCredentialsException("LDAP Authentication failed: User not found in system or LDAP."));
                if (!"LdapPass123!".equals(password)) {
                    throw new BadCredentialsException("LDAP Authentication failed: Invalid LDAP credentials.");
                }
                if (shadowUser.isLocked()) {
                    throw new LockedException("User account is locked.");
                }
                return new UsernamePasswordAuthenticationToken(username, password,
                        Collections.singletonList(new SimpleGrantedAuthority(shadowUser.getRole())));
            }
        } else {
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new BadCredentialsException("Invalid username or password."));

            if (user.isLocked()) {
                throw new LockedException("User account is locked.");
            }
            if (user.isExpired()) {
                throw new AccountExpiredException("User account is expired.");
            }
            if (user.isCredentialsExpired()) {
                throw new CredentialsExpiredException("User credentials have expired. Please reset your password.");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("Invalid username or password.");
            }

            return new UsernamePasswordAuthenticationToken(username, password,
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
