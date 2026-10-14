package com.example.ecommercedemo.api.carts.items;

import com.example.ecommercedemo.components.carts.CartCookie;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.entities.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RequestMapping("api/cart/items")
@Tag(name = "Cart items")
public interface ItemAPI {

    /** Command: adding an item starts a cart on first use (and sets the guest cookie). */
    @PostMapping
    @Operation(summary = "Adds an item to the current cart, creating the cart on first use")
    ResponseEntity<CartDTO> addCartItem(
            @RequestBody CreateItemDTO request,
            @CookieValue(name = CartCookie.NAME, required = false) UUID suid,
            @AuthenticationPrincipal User user,
            HttpServletResponse response
    );

    @DeleteMapping
    @Operation(summary = "Decrements an item quantity; requires an existing cart")
    ResponseEntity<CartDTO> decrementCartItem(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @CookieValue(name = CartCookie.NAME, required = false) UUID suid,
            @AuthenticationPrincipal User user
    );

    @DeleteMapping("/all")
    @Operation(summary = "Removes an item regardless of quantity; requires an existing cart")
    ResponseEntity<CartDTO> removeCartItem(
            @RequestParam Long productId,
            @CookieValue(name = CartCookie.NAME, required = false) UUID suid,
            @AuthenticationPrincipal User user
    );
}
