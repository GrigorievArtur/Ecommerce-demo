package com.example.ecommercedemo.controllers.carts;

import com.example.ecommercedemo.api.carts.CartAPI;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateCartItemDTO;
import com.example.ecommercedemo.services.carts.CartItemService;
import com.example.ecommercedemo.services.carts.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CartController implements CartAPI {

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private CartService cartService;

    @Override
    public ResponseEntity<CartDTO> getCart(UUID suid) {
        return ResponseEntity.ok(cartService.getCartDTO(suid));
    }

    @Override
    public ResponseEntity<CartDTO> addCartItem(CreateCartItemDTO request, UUID suid) {
        return ResponseEntity.ok(cartItemService.addCartItem(suid, request));
    }

    @Override
    public ResponseEntity<CartDTO> decrementCartItem(Long productId, int quantity, UUID suid) {
        return ResponseEntity.ok(cartItemService.decrementCartItem(productId, quantity, suid));
    }

    @Override
    public ResponseEntity<CartDTO> removeCartItem(Long productId, UUID suid) {
        return ResponseEntity.ok(cartItemService.removeCartItem(suid, productId));
    }
}
