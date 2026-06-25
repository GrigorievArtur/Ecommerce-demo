package com.example.ecommercedemo.services.carts;

import com.example.ecommercedemo.components.auth.SecurityHelper;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.mappers.carts.CartMapper;
import com.example.ecommercedemo.models.pricing.BasePrice;
import com.example.ecommercedemo.models.pricing.LinePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import com.example.ecommercedemo.models.pricing.frozen.FrozenLinePrice;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.items.ItemService;
import com.example.ecommercedemo.services.pricing.PriceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private SecurityHelper securityHelper;

    @Autowired
    private PriceService priceService;

    public CartDTO getCartDTO(UUID suid) {
        Cart cart = getCart(suid);
        return getCartDTO(cart);
    }

    public CartDTO getCartDTO(Cart cart) {
        CartDTO dto = cartMapper.cartToCartDTO(cart);
        Map<Long, Product> productMap = productRepo.findAllById(new ArrayList<>(cart.getItems().keySet()))
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        dto.setItems(

                cart.getItems().entrySet().stream()
                        .map(entry -> {
                            return itemService.toDTO(entry.getKey(), entry.getValue(), productMap);
                        })
                        .toList()
        );

        UnitPrice price = calculatePriceData(cart, productMap);
        dto.setPrice(price);

        return dto;
    }

    public CartDTO addItemToCart(CreateItemDTO request, UUID suid) {
        var cart = getCart(suid);
        itemService.addItem(cart.getItems(), request);
        var savedCart = cartRepo.save(cart);
        return getCartDTO(savedCart);
    }

    public CartDTO decreaseItemFromCart(Long productId, int quantity, UUID suid) {
        var cart = getCart(suid);
        itemService.decrementItem(cart.getItems(), productId, quantity);
        var savedCart = cartRepo.save(cart);
        return getCartDTO(savedCart);
    }

    public CartDTO removeItemFromCart(Long productId, UUID suid) {
        var cart = getCart(suid);
        itemService.removeItem(cart.getItems(), productId);
        var savedCart = cartRepo.save(cart);
        return getCartDTO(savedCart);
    }

//  Internal shi code

    public Cart getCart(UUID suid) {
        return securityHelper.getCurrentUser()
                .map(user -> getUserCart(user, suid))
                .orElseGet(() -> getGuestCart(suid));
    }

    private Cart getUserCart(User user, UUID suid) {
        Optional<Cart> userCart = cartRepo.findByUserId(user.getId());

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

    // Just calculation of price for the cart
    private UnitPrice calculatePriceData(Cart cart, Map<Long, Product> productMap) {
        BigDecimal cartTotal = BigDecimal.ZERO;

        for (Map.Entry<Long, FrozenLinePrice> entry : cart.getItems().entrySet()) {
            Long productId = entry.getKey();
            Product product = productMap.get(productId); // guaranteed to exist

            FrozenLinePrice frozenLinePrice = entry.getValue();
            var currentQty = (frozenLinePrice != null) ? frozenLinePrice.getQuantity() : 1;

            frozenLinePrice = priceService.refreshSnapshot(frozenLinePrice, () ->
                    LinePrice.builder()
                            .quantity(currentQty)
                            .unitPrice(product.getUnitPrice())
                            .build()
            );

            entry.setValue(frozenLinePrice);
            cartTotal = cartTotal.add(frozenLinePrice.effectivePrice());
        }

        return UnitPrice.builder()
                .basePrice(BasePrice.builder().price(cartTotal).build())
                .discountPercentage(BigDecimal.ZERO)
                .build();
    }
}