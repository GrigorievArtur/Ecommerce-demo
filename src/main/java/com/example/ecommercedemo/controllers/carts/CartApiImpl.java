package com.example.ecommercedemo.controllers.carts;

import com.example.ecommercedemo.api.carts.CartAPI;
import com.example.ecommercedemo.components.carts.CartCookie;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.CreateCartDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.models.carts.CartOwner;
import com.example.ecommercedemo.services.carts.CartService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@Transactional
@RequiredArgsConstructor
public class CartApiImpl implements CartAPI {

    private final CartService cartService;

    @Override
    public ResponseEntity<CartDTO> getCart(UUID suid, User user) {
        return cartService.findCart(CartOwner.of(user, suid))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<CartDTO> createCart(UUID suid, User user, CreateCartDTO createCartDTO, HttpServletResponse response) {
        CartOwner owner = CartOwner.of(user, suid);
        if (cartService.findCart(owner).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        CreateCartDTO body = createCartDTO != null ? createCartDTO : new CreateCartDTO();
        CartDTO cart = cartService.createCart(owner, body);
        CartCookie.write(response, cart.getSuid());
        return ResponseEntity.created(URI.create("/api/cart")).body(cart);
    }
}
