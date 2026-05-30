package com.example.ecommercedemo.components.auth;

import com.example.ecommercedemo.entities.users.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityHelper {

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return Optional.of(user);
        }

        return Optional.empty();
    }

    public boolean isLoggedIn() {
        return getCurrentUser().isPresent();
    }

    public Long getCurrentUserIdOrThrow() {
        return getCurrentUser()
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }

    public User getCurrentUserOrThrow() {
        return getCurrentUser().orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }
}