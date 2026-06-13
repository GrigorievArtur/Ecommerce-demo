package com.example.ecommercedemo.services.carts;

import com.example.ecommercedemo.components.auth.SecurityHelper;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.items.CreateItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.mappers.carts.CartMapper;
import com.example.ecommercedemo.models.pricing.BasePrice;
import com.example.ecommercedemo.models.pricing.LinePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import com.example.ecommercedemo.models.pricing.frozen.FrozenLinePrice;
import com.example.ecommercedemo.models.pricing.frozen.FrozenPrice;
import com.example.ecommercedemo.models.pricing.frozen.FrozenUnitPrice;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.items.ItemService;
import com.example.ecommercedemo.services.pricing.PriceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

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

        dto.setItems(
                cart.getItems().stream()
                        .map(itemService::toDTO)
                        .toList()
        );

        UnitPrice price = calculatePriceData(cart);
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

//    Just calculation of price for the cart
    private UnitPrice calculatePriceData(Cart cart) {
        BigDecimal cartTotal = BigDecimal.ZERO;

        for (var item : cart.getItems()) {
            FrozenLinePrice linePrice = item.getFrozenLinePrice();

            var currentQty = (linePrice != null) ? linePrice.getQuantity() : 1;

            linePrice = priceService.refreshSnapshot(linePrice, () -> {

                UnitPrice liveProductPrice = productRepo.findById(item.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product pricing details missing"))
                        .getUnitPrice();

                return LinePrice.builder()
                        .quantity(currentQty)
                        .unitPrice(liveProductPrice)
                        .build();
            });

            item.setFrozenLinePrice(linePrice); // update back into live collection reference
            cartTotal = cartTotal.add(linePrice.effectivePrice()); // Sum up the pre-calculated line totals securely
        }

        return UnitPrice.builder()
                .basePrice(BasePrice.builder().price(cartTotal).build())
                .discountPercentage(BigDecimal.ZERO)
                .build();
    }
}