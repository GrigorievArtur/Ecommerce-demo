package com.example.ecommercedemo.services.carts;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateCartItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.mappers.carts.items.CartItemMapper;
import com.example.ecommercedemo.mappers.carts.CartMapper;
import com.example.ecommercedemo.models.carts.CartItemModel;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        var item = findCartItemModelInCart(cart, cartItemDTO.getProductId());

        if (item == null) {
            cart.getItems().add(cartItemMapper.toModel(cartItemDTO));
        } else {
            item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
        }

        var savedCart = cartRepo.save(cart);
        return cartMapper.cartToCartDTO(savedCart);
    }

        public CartDTO decrementCartItem(Long productId, int quantity, UUID suid) {
            var cart = cartService.getCart(suid);
            var item = findCartItemModelInCart(cart, productId);

            if (item == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not in cart");
            }

            int remaining = item.getQuantity() - quantity;

            if (remaining <= 0) {
                cart.getItems().remove(item);
            } else {
                item.setQuantity(remaining);
            }

            var savedCart = cartRepo.save(cart);
            return cartMapper.cartToCartDTO(savedCart);
        }

        public CartDTO removeCartItem(UUID suid, Long productId) {
            var cart = cartService.getCart(suid);
            var item = findCartItemModelInCart(cart, productId);

            if (item == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart");
            }

            cart.getItems().remove(item);


            var savedCart = cartRepo.save(cart);
            return cartMapper.cartToCartDTO(savedCart);
        }




    private CartItemModel findCartItemModelInCart(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

}
