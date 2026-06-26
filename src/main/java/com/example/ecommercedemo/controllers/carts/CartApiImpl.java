package com.example.ecommercedemo.controllers.carts;

import com.example.ecommercedemo.api.carts.CartAPI;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.CreateCartDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.services.carts.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Transactional
public class CartApiImpl implements CartAPI {

    @Autowired
    private CartService cartService;

    @Override
    public ResponseEntity<CartDTO> getCart(UUID suid) {
        return ResponseEntity.ok(cartService.getCartDTO(suid));
    }

    //how to make this run even if unthoneticated cause i need user to be null sometimes.
    @Override
    public ResponseEntity<CartDTO> createCart(@RequestBody CreateCartDTO createCartDTO, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.createCart(createCartDTO, user));
    }

}
