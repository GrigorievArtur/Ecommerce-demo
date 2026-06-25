package com.example.ecommercedemo.services.carts;

import com.example.ecommercedemo.components.auth.SecurityHelper;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.carts.CartItem;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.mappers.carts.CartMapper;
import com.example.ecommercedemo.models.pricing.Price;
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

        Set<Long> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());
        Map<Long, Product> productMap = productRepo.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        dto.setItems(
                cart.getItems().stream()
                        .map(item -> itemService.toDTO(item, productMap))
                        .toList()
        );

        Price total = calculateCartTotal(cart, productMap);
        dto.setPrice(total);

        return dto;
    }

    public CartDTO addItemToCart(CreateItemDTO request, UUID suid) {
        var cart = getCart(suid);
        itemService.addItem(cart, request);
        var savedCart = cartRepo.save(cart);
        return getCartDTO(savedCart);
    }

    public CartDTO decreaseItemFromCart(Long productId, int quantity, UUID suid) {
        var cart = getCart(suid);
        itemService.decrementItem(cart, productId, quantity);
        var savedCart = cartRepo.save(cart);
        return getCartDTO(savedCart);
    }

    public CartDTO removeItemFromCart(Long productId, UUID suid) {
        var cart = getCart(suid);
        itemService.removeItem(cart, productId);
        var savedCart = cartRepo.save(cart);
        return getCartDTO(savedCart);
    }

    // --- Internal cart resolution ---

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

    // --- Cart total calculation ---

    private Price calculateCartTotal(Cart cart, Map<Long, Product> productMap) {
        BigDecimal cartTotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null) continue;

            int currentQty = item.getPriceSnapshot().getQuantity().intValue();

            item.setPriceSnapshot(
                    priceService.refreshSnapshot(item.getPriceSnapshot(), () ->
                            product.getPrice().toBuilder()
                                    .quantity(BigDecimal.valueOf(currentQty))
                                    .build()
                    )
            );

            cartTotal = cartTotal.add(priceService.effectivePrice(item.getPriceSnapshot()));
        }

        return Price.builder()
                .grossPrice(cartTotal)
                .percentageDiscount(BigDecimal.ZERO)
                .quantity(BigDecimal.ONE)
                .build();
    }
}
