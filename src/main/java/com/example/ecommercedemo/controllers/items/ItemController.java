package com.example.ecommercedemo.controllers.items;

import com.example.ecommercedemo.api.items.ItemAPI;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.items.CreateItemDTO;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.items.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ItemController implements ItemAPI {

    @Autowired
    private CartService cartService;

    @Override
    public ResponseEntity<CartDTO> addCartItem(CreateItemDTO request, UUID suid) {
        return ResponseEntity.ok(cartService.addItemToCart(request, suid));
    }

    @Override
    public ResponseEntity<CartDTO> decrementCartItem(Long productId, int quantity, UUID suid) {
        return ResponseEntity.ok(cartService.decreaseItemFromCart(productId, quantity, suid));
    }

    @Override
    public ResponseEntity<CartDTO> removeCartItem(Long productId, UUID suid) {
        return ResponseEntity.ok(cartService.removeItemFromCart(productId, suid));
    }
}
