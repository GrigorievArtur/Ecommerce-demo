package com.example.ecommercedemo.api.items;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.items.CreateItemDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("api/cart/items/")
@Tag(name = "Carts of different users")
public interface ItemAPI {


    @PostMapping
    @Operation(summary = "Adds cart items to the current cart")
    ResponseEntity<CartDTO> addCartItem(
            @RequestBody CreateItemDTO request,
            @CookieValue(name = "suid", required = false) UUID suid
    );

    @DeleteMapping
    @Operation(summary = "Decrements cart items using quantity param")
    ResponseEntity<CartDTO> decrementCartItem(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @CookieValue(name = "suid", required = false) UUID suid
    );

    @DeleteMapping("/all")
    @Operation(summary = "Removes a cart item from the current cart no matter the quantity.")
    ResponseEntity<CartDTO> removeCartItem(
            @RequestParam Long productId,
            @CookieValue(name = "suid", required = false) UUID suid
    );


}