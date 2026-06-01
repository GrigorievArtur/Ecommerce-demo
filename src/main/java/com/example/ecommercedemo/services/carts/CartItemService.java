package com.example.ecommercedemo.services.carts;

import com.example.ecommercedemo.components.auth.SecurityHelper;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateCartItemDTO;
import com.example.ecommercedemo.dtos.carts.items.UpdateCartItemDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.mappers.carts.items.CartItemMapper;
import com.example.ecommercedemo.mappers.carts.CartMapper;
import com.example.ecommercedemo.mappers.products.ProductMapper;
import com.example.ecommercedemo.models.carts.CartItemModel;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CartItemService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private CartService cartService;

    public CartDTO addCartItem(UUID suid, CreateCartItemDTO cartItemDTO) {
        var cart = cartService.getCart(suid);
        cart.getItems().add(cartItemMapper.toModel(cartItemDTO));
        var savedCart = cartRepo.save(cart);
        return cartMapper.cartToCartDTO(savedCart);
    }

    public CartDTO deleteCartItem(UUID suid, Long productId) {
        var cart = cartService.getCart(suid);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        var savedCart = cartRepo.save(cart);
        return cartMapper.cartToCartDTO(savedCart);
    }

    public CartDTO updateCartItem(UUID suid, Long productId, UpdateCartItemDTO updateCartItemDTO) {
        var cart = cartService.getCart(suid);

        var item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));

        cartItemMapper.updateModelFromDTO(updateCartItemDTO, item);

        var savedCart = cartRepo.save(cart);
        return cartMapper.cartToCartDTO(savedCart);
    }

}
