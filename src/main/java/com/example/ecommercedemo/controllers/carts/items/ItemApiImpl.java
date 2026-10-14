package com.example.ecommercedemo.controllers.carts.items;

import com.example.ecommercedemo.api.carts.items.ItemAPI;
import com.example.ecommercedemo.components.carts.CartCookie;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.models.carts.CartOwner;
import com.example.ecommercedemo.services.carts.CartService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Transactional
@RequiredArgsConstructor
public class ItemApiImpl implements ItemAPI {

    private final CartService cartService;

    @Override
    public ResponseEntity<CartDTO> addCartItem(CreateItemDTO request, UUID suid, User user, HttpServletResponse response) {
        CartDTO cart = cartService.addItem(CartOwner.of(user, suid), request);
        CartCookie.write(response, cart.getSuid());
        return ResponseEntity.ok(cart);
    }

    @Override
    public ResponseEntity<CartDTO> decrementCartItem(Long productId, int quantity, UUID suid, User user) {
        return ResponseEntity.ok(cartService.decrementItem(CartOwner.of(user, suid), productId, quantity));
    }

    @Override
    public ResponseEntity<CartDTO> removeCartItem(Long productId, UUID suid, User user) {
        return ResponseEntity.ok(cartService.removeItem(CartOwner.of(user, suid), productId));
    }
}
