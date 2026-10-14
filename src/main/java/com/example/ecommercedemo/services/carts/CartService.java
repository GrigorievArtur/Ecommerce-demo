package com.example.ecommercedemo.services.carts;

import com.example.ecommercedemo.components.auth.SecurityHelper;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.CreateCartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.carts.CartItem;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.mappers.carts.CartMapper;
import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.models.pricing.PriceSnapshot;
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
    private PriceService priceService;

    @Autowired
    private SecurityHelper securityHelper;

    // ── Read ──────────────────────────────────────────────────────

    public CartDTO getCartDTO(UUID suid) {
        Cart cart = resolveCart(suid);
        return getCartDTO(cart);
    }

    /**
     * Returns the current cart (user cart when authenticated, else guest cart by suid),
     * creating a fresh empty cart when none exists yet.
     */
    public CartDTO getOrCreateCartDTO(UUID suid, User user) {
        return securityHelper.getCurrentUser()
                .flatMap(u -> cartRepo.findByUserId(u.getId()))
                .or(() -> cartRepo.findBySuid(suid))
                .map(this::getCartDTO)
                .orElseGet(() -> createCart(new CreateCartDTO(), user));
    }

    /** Empties the current cart, e.g. after a successful checkout. */
    public void clearCart(UUID suid) {
        emptyCart(resolveCart(suid));
    }

    public void emptyCart(Cart cart) {
        cart.getItems().clear();
        cartRepo.save(cart);
    }

    /**
     * Fails with 409 when the cart holds more units than are currently tracked in stock.
     * Re-checked at checkout because stock can change while a cart sits around.
     */
    public void assertItemsInStock(Cart cart) {
        Map<Long, Product> productMap = loadProductMap(cart);
        for (CartItem item : cart.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null || product.getStock() == null) {
                continue;
            }
            int requested = item.getPriceSnapshot().getQuantity().intValue();
            if (requested > product.getStock()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Only " + product.getStock() + " left in stock for " + product.getName());
            }
        }
    }

    /**
     * Merges the guest cart identified by the {@code suid} cookie into the signed-in user's
     * cart. Safe to call on every login: it is a no-op when no guest cart exists.
     */
    public void mergeGuestCartIntoUserCart(UUID suid, User user) {
        if (suid == null || user == null) {
            return;
        }
        Cart guestCart = cartRepo.findBySuid(suid).orElse(null);
        if (guestCart == null || guestCart.getUser() != null) {
            return;
        }
        Cart userCart = cartRepo.findByUserId(user.getId()).orElse(null);
        if (userCart == null) {
            guestCart.setUser(user);
            cartRepo.save(guestCart);
            return;
        }
        if (Objects.equals(userCart.getId(), guestCart.getId())) {
            return;
        }
        if (guestCart.getItems().isEmpty()) {
            cartRepo.delete(guestCart);
            return;
        }

        Set<Long> productIds = new HashSet<>();
        userCart.getItems().forEach(item -> productIds.add(item.getProductId()));
        guestCart.getItems().forEach(item -> productIds.add(item.getProductId()));
        Map<Long, Product> productMap = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (CartItem guestItem : guestCart.getItems()) {
            int quantity = guestItem.getPriceSnapshot().getQuantity().intValue();
            try {
                itemService.addItem(userCart, CreateItemDTO.builder()
                        .productId(guestItem.getProductId())
                        .quantity(quantity)
                        .build(), productMap);
            } catch (ResponseStatusException outOfStock) {
                // Keep whatever fits; never block a successful sign-in on a stock limit.
            }
        }
        cartRepo.save(userCart);
        cartRepo.delete(guestCart);
    }

    public CartDTO getCartDTO(Cart cart) {
        Map<Long, Product> productMap = loadProductMap(cart);
        if (itemService.purgeOrphans(cart, productMap)) {
            cartRepo.save(cart);
        }
        return buildCartDTO(cart, productMap);
    }

    // ── Create ────────────────────────────────────────────────────

    /** Creates cart + items in one shot. Returns DTO directly to reuse productMap. */
    public CartDTO createCart(CreateCartDTO dto, User user) {
        Cart cart = Cart.builder().user(user).build();

        List<CreateItemDTO> itemDTOs = Optional.ofNullable(dto.getItems())
                .orElse(Collections.emptyList());

        if (itemDTOs.isEmpty()) {
            return buildCartDTO(cartRepo.save(cart), Collections.emptyMap());
        }

        Map<Long, Product> productMap = loadProductMap(itemDTOs);

        List<CartItem> items = itemDTOs.stream()
                .map(item -> {
                    CartItem cartItem = itemService.toCartItem(item, productMap);
                    cartItem.setCart(cart);
                    return cartItem;
                })
                .toList();

        cart.setItems(items);
        return buildCartDTO(cartRepo.save(cart), productMap);
    }

    // ── Modify ────────────────────────────────────────────────────

    public CartDTO addItemToCart(CreateItemDTO request, UUID suid) {
        Cart cart = resolveCart(suid);
        Map<Long, Product> productMap = loadProductMapForAdd(cart, request.getProductId());
        itemService.addItem(cart, request, productMap);
        return buildCartDTO(cartRepo.save(cart), productMap);
    }

    public CartDTO decreaseItemFromCart(Long productId, int quantity, UUID suid) {
        Cart cart = resolveCart(suid);
        Map<Long, Product> productMap = loadProductMap(cart);
        itemService.decrementItem(cart, productId, quantity, productMap);
        return buildCartDTO(cartRepo.save(cart), productMap);
    }

    public CartDTO removeItemFromCart(Long productId, UUID suid) {
        Cart cart = resolveCart(suid);
        Map<Long, Product> productMap = loadProductMap(cart);
        itemService.removeItem(cart, productId);
        return buildCartDTO(cartRepo.save(cart), productMap);
    }

    // ── Internal ──────────────────────────────────────────────────

    /** Resolve cart: user cart by userId, or guest cart by suid. Throws 404 if neither found. */
    public Cart resolveCart(UUID suid) {
        return securityHelper.getCurrentUser()
                .flatMap(user -> cartRepo.findByUserId(user.getId()))
                .or(() -> cartRepo.findBySuid(suid))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
    }

    // needs to be public
    public Map<Long, Product> loadProductMap(Cart cart) {
        Set<Long> ids = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) return Collections.emptyMap();
        return productRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private Map<Long, Product> loadProductMap(List<CreateItemDTO> itemDTOs) {
        Set<Long> ids = itemDTOs.stream()
                .map(CreateItemDTO::getProductId)
                .collect(Collectors.toSet());
        return productRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private CartDTO buildCartDTO(Cart cart, Map<Long, Product> productMap) {
        CartDTO dto = cartMapper.cartToCartDTO(cart);
        dto.setItems(
                cart.getItems().stream()
                        .map(item -> itemService.toDTO(item, productMap))
                        .filter(Objects::nonNull)
                        .toList()
        );
        dto.setPrice(calculateCartTotal(cart, productMap));
        return dto;
    }

    private Price calculateCartTotal(Cart cart, Map<Long, Product> productMap) {
        BigDecimal cartTotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null) continue;

            int qty = item.getPriceSnapshot().getQuantity().intValue();
            PriceSnapshot effective = priceService.refreshSnapshot(item.getPriceSnapshot(), () ->
                    product.getPrice().toBuilder()
                            .quantity(BigDecimal.valueOf(qty))
                            .build()
            );
            cartTotal = cartTotal.add(PriceService.effectivePrice(effective));
        }

        return Price.builder()
                .grossPrice(cartTotal)
                .percentageDiscount(BigDecimal.ZERO)
                .quantity(BigDecimal.ONE)
                .build();
    }

    private Map<Long, Product> loadProductMapForAdd(Cart cart, Long newProductId) {
        Set<Long> ids = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toCollection(HashSet::new));
        ids.add(newProductId);
        if (ids.isEmpty()) return Collections.emptyMap();
        return productRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }
}
