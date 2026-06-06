package com.example.ecommercedemo.api.carts;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
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



}