package com.example.ecommercedemo.services.carts;

import com.example.ecommercedemo.components.auth.SecurityHelper;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.mappers.items.ItemMapper;
import com.example.ecommercedemo.mappers.carts.CartMapper;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private SecurityHelper securityHelper;


    // Probably is not redundant 'cause business logic
    public CartDTO getCartDTO(UUID suid) {
        Cart cart = getCart(suid);
        return getCartDTO(cart);
    }

    public CartDTO getCartDTO(Cart cart) {
        CartDTO dto = cartMapper.cartToCartDTO(cart);

        dto.setTotalPrice(calculateBasePrice(cart));
        dto.setFinalPrice(calculateSalePrice(cart));

        return dto;
    }

    // Internal stuff
    public Cart getCart(UUID suid) {
        return securityHelper.getCurrentUser()
                .map(user -> getUserCart(user, suid))
                .orElseGet(() -> getGuestCart(suid));
    }

    private Cart getUserCart(User user, UUID suid) {
        Optional<Cart> userCart = cartRepo.findByUser_Id(user.getId());

        if (suid == null) {
            return userCart.orElseGet(() -> createUserCart(user));
        }

        Optional<Cart> guestCart = cartRepo.findBySuid(suid);

        if (guestCart.isEmpty()) {
            return userCart.orElseGet(() -> createUserCart(user));
        }

        Cart suidCart = guestCart.get();

        if (suidCart.getUser() != null && suidCart.getUser().getId().equals(user.getId())) {
            return suidCart;
        }

        if (userCart.isEmpty()) {
            suidCart.setUser(user);
            suidCart.setExpiryDate(null);
            return cartRepo.save(suidCart);
        }

        return userCart.get();
    }

    private Cart getGuestCart(UUID suid) {
        if (suid == null) {
            return createGuestCart();
        }

        return cartRepo.findBySuid(suid)
                .orElseGet(this::createGuestCart);
    }

    private Cart createGuestCart() {
        return cartRepo.save(new Cart());
    }

    private Cart createUserCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setExpiryDate(null);
        return cartRepo.save(cart);
    }



    // This looks so bad 😭
    public BigDecimal calculateBasePrice(Cart cart) {
        return cart.getItems().stream().map(cartItem ->
        {
          BigDecimal productPrice = productRepo.findById(cartItem.getProductId())
                  .orElseThrow().getSalePrice();
          return productPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateSalePrice(Cart cart){
        BigDecimal totalPrice = calculateBasePrice(cart);
        BigDecimal discountPercentage = cart.getDiscountPercentage() == null ? BigDecimal.ZERO : cart.getDiscountPercentage();
        BigDecimal discountAmount = totalPrice.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        return totalPrice.subtract(discountAmount);
    }



}
