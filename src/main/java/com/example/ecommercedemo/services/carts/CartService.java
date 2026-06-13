package com.example.ecommercedemo.services.carts;

import com.example.ecommercedemo.components.auth.SecurityHelper;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.items.CreateItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.mappers.carts.CartMapper;
import com.example.ecommercedemo.models.common.PriceData;
import com.example.ecommercedemo.models.common.PriceSnapshot;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import com.example.ecommercedemo.models.pricing.snapshots.SnapshotUnitPrice;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.items.ItemService;
import com.example.ecommercedemo.services.pricing.PriceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
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


    // Probably is not redundant 'cause business logic
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

    // Items manipulation stuff
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



    // This looks so bad 😭 checks if snapshot is valid and calculates total
    private UnitPrice calculatePriceData(Cart cart) {
        UnitPrice cartPrice = new UnitPrice();
        cartPrice.getBasePrice().setPrice(BigDecimal.ZERO);

        cart.getItems().forEach(item -> {

            //refreshing the shi if its stale.
            SnapshotUnitPrice priceSnapshot = item.getLinePrice().getSnapshotUnitPrice();
            if (priceService.isSnapshotStale(priceSnapshot)) {
                priceSnapshot = SnapshotUnitPrice.from(productRepo.findById(item.getProductId())
                        .orElseThrow().getUnitPrice()
                );
                item.getLinePrice().setSnapshotUnitPrice(priceSnapshot);
            }


            cartPrice.getBasePrice().setPrice(item.getLinePrice().effectivePrice());
        });

        return priceService.applyDiscount(cartPrice);
    }

}
