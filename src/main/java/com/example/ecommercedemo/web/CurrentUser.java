package com.example.ecommercedemo.web;

import com.example.ecommercedemo.entities.users.User;
import lombok.Getter;

/**
 * Trimmed view of the authenticated user for templates — never exposes the password hash.
 */
@Getter
public class CurrentUser {

    private final Long id;
    private final String firstname;
    private final String lastname;
    private final String email;
    private final boolean admin;

    public CurrentUser(Long id, String firstname, String lastname, String email, boolean admin) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.admin = admin;
    }

    public String getDisplayName() {
        return firstname;
    }

    public static CurrentUser from(User user) {
        return new CurrentUser(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getRole() != null && user.getRole().name().equals("ADMIN")
        );
    }
}
