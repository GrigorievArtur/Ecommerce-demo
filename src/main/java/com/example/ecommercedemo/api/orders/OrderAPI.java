
package com.example.ecommercedemo.api.orders;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.entities.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RequestMapping("api/order")
@Tag(name = "Orders of different users")
public interface OrderAPI {

    @PostMapping
    @Operation(
            summary = "Gets the current cart and saves it as order"
    )
    ResponseEntity<CartDTO> saveOrder(
            @CookieValue(name = "suid", required = false) UUID suid,
            @AuthenticationPrincipal User user
    );

// TODO This might be safe to delete later.

}