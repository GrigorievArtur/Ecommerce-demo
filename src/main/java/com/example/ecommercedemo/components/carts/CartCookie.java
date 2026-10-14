package com.example.ecommercedemo.components.carts;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

/**
 * Single place that reads and writes the guest cart cookie, so the web controllers, the
 * login handler and the REST API all agree on the cookie name and lifetime.
 */
public final class CartCookie {

    public static final String NAME = "suid";

    private static final int THIRTY_DAYS_SECONDS = 60 * 60 * 24 * 30;

    private CartCookie() {
    }

    /** Persists the cart id on the browser. No-op when there is nothing to write. */
    public static void write(HttpServletResponse response, UUID suid) {
        if (response == null || suid == null) {
            return;
        }
        Cookie cookie = new Cookie(NAME, suid.toString());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(THIRTY_DAYS_SECONDS);
        response.addCookie(cookie);
    }

    /** Reads the cart id, returning {@code null} for a missing or malformed cookie. */
    public static UUID read(HttpServletRequest request) {
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (NAME.equals(cookie.getName())) {
                try {
                    return UUID.fromString(cookie.getValue());
                } catch (IllegalArgumentException malformed) {
                    return null;
                }
            }
        }
        return null;
    }
}
