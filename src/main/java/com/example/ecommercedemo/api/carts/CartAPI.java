package com.example.ecommercedemo.api.carts;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.CreateCartDTO;
import com.example.ecommercedemo.entities.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RequestMapping("api/cart")
@Tag(name = "Carts of different users")
public interface CartAPI {

    @GetMapping
    @Operation(
            summary = "Gets the current cart",
            description = "Gets the guest or user cart depending on whether the user is logged in. Also merges carts if the user logged in after making a guest cart."
    )
    ResponseEntity<CartDTO> getCart(
            @CookieValue(name = "suid", required = false) UUID suid
    );

    @PostMapping
    @Operation(
            summary = "creates a cart for current user/guest"
    )
    ResponseEntity<CartDTO> createCart(@RequestBody CreateCartDTO createCartDTO, @AuthenticationPrincipal User user);

}