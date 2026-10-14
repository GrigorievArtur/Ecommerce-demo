package com.example.ecommercedemo.models.carts;

import com.example.ecommercedemo.entities.users.User;

import java.util.UUID;

/**
 * Identifies who a cart belongs to.
 *
 * <p>Guest and signed-in ownership are deliberately different types, so the cart service
 * never has to guess identity from the security context. A guest owns exactly one cart per
 * {@code suid} cookie; an account owns exactly one cart per user.</p>
 */
public sealed interface CartOwner {

    /** Anonymous shopper. The {@code suid} is {@code null} until a cart has been created. */
    record Guest(UUID suid) implements CartOwner {
    }

    /** Signed-in shopper. */
    record Account(User user) implements CartOwner {
    }

    /** Resolves the owner for a request: an authenticated principal wins over the cookie. */
    static CartOwner of(User user, UUID suid) {
        return user != null ? new Account(user) : new Guest(suid);
    }
}
