package com.example.ecommercedemo.api.carts;

import com.example.ecommercedemo.components.carts.CartCookie;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.CreateCartDTO;
import com.example.ecommercedemo.entities.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequestMapping("api/cart")
@Tag(name = "Carts")
public interface CartAPI {

    /**
     * Reads the caller's cart. GET never creates one: a caller with no cart gets 404 and
     * must call {@code POST /api/cart} first.
     */
    @GetMapping
    @Operation(
            summary = "Gets the current cart",
            description = "Returns the guest or account cart, or 404 when the caller has none. Read-only."
    )
    ResponseEntity<CartDTO> getCart(
            @CookieValue(name = CartCookie.NAME, required = false) UUID suid,
            @AuthenticationPrincipal User user
    );

    /**
     * Creates the caller's cart. Kept separate from {@link #getCart} so creation is an
     * explicit action; a caller that already owns a cart gets 409 Conflict.
     */
    @PostMapping
    @Operation(
            summary = "Creates the current cart",
            description = "Creates an empty (or pre-filled) cart, or 409 when the caller already has one."
    )
    ResponseEntity<CartDTO> createCart(
            @CookieValue(name = CartCookie.NAME, required = false) UUID suid,
            @AuthenticationPrincipal User user,
            @RequestBody(required = false) CreateCartDTO createCartDTO,
            HttpServletResponse response
    );
}
