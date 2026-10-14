package com.example.ecommercedemo.services.carts;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.CreateCartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.carts.CartItem;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.mappers.carts.CartMapper;
import com.example.ecommercedemo.models.carts.CartOwner;
import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.models.pricing.PriceSnapshot;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.items.ItemService;
import com.example.ecommercedemo.services.pricing.PriceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Owns the cart lifecycle.
 *
 * <p>Every method takes a {@link CartOwner}, so guest and account carts follow the same
 * code path but are never conflated. Reading ({@link #findCart}) is side-effect free and
 * never creates a cart; creation is explicit via {@link #createCart}; item mutations are
 * commands and may start a cart on first use.</p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartRepo cartRepo;
    private final CartMapper cartMapper;
    private final ItemService itemService;
    private final ProductRepo productRepo;
    private final PriceService priceService;

    // ── Read ──────────────────────────────────────────────────────

    /**
     * Read-only lookup. Never creates a cart and never writes to the database, so it is
     * safe to call from a GET, from an error page or from layout rendering.
     */
    public Optional<CartDTO> findCart(CartOwner owner) {
        return findCartEntity(owner).map(this::toCartDTO);
    }

    public Optional<Cart> findCartEntity(CartOwner owner) {
        if (owner instanceof CartOwner.Guest guest) {
            return guest.suid() == null
                    ? Optional.empty()
                    : cartRepo.findBySuid(guest.suid());
        }
        if (owner instanceof CartOwner.Account account) {
            return cartRepo.findByUserId(account.user().getId());
        }
        throw new IllegalArgumentException("Unknown cart owner: " + owner);
    }

    /** Like {@link #findCartEntity} but fails with 404 instead of returning empty. */
    public Cart requireCartEntity(CartOwner owner) {
        return findCartEntity(owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
    }

    // ── Create ────────────────────────────────────────────────────

    /**
     * Explicitly creates a cart, optionally with initial items. The caller decides what to
     * do when a cart already exists (the REST endpoint answers 409).
     */
    public CartDTO createCart(CartOwner owner, CreateCartDTO dto) {
        Cart cart = createCartEntity(owner);

        List<CreateItemDTO> itemDTOs = Optional.ofNullable(dto.getItems())
                .orElse(Collections.emptyList());
        if (itemDTOs.isEmpty()) {
            return toCartDTO(cart);
        }

        Map<Long, Product> productMap = loadProductMap(itemDTOs);
        List<CartItem> items = new ArrayList<>();
        for (CreateItemDTO item : itemDTOs) {
            CartItem cartItem = itemService.toCartItem(item, productMap);
            cartItem.setCart(cart);
            items.add(cartItem);
        }
        cart.setItems(items);
        return buildCartDTO(cartRepo.save(cart), productMap);
    }

    private Cart createCartEntity(CartOwner owner) {
        Cart cart = owner instanceof CartOwner.Account account
                ? Cart.builder().user(account.user()).build()
                : Cart.builder().build();
        return cartRepo.save(cart);
    }

    // ── Modify ────────────────────────────────────────────────────

    /**
     * Adds an item to the owner's cart, starting one on first use. This is a command, so
     * the implicit creation is intentional — a read never does this.
     */
    public CartDTO addItem(CartOwner owner, CreateItemDTO request) {
        Cart cart = findCartEntity(owner).orElseGet(() -> createCartEntity(owner));
        Map<Long, Product> productMap = loadProductMapForAdd(cart, request.getProductId());
        itemService.addItem(cart, request, productMap);
        return buildCartDTO(cartRepo.save(cart), productMap);
    }

    public CartDTO decrementItem(CartOwner owner, Long productId, int quantity) {
        Cart cart = requireCartEntity(owner);
        Map<Long, Product> productMap = loadProductMap(cart);
        itemService.decrementItem(cart, productId, quantity, productMap);
        return buildCartDTO(cartRepo.save(cart), productMap);
    }

    public CartDTO removeItem(CartOwner owner, Long productId) {
        Cart cart = requireCartEntity(owner);
        Map<Long, Product> productMap = loadProductMap(cart);
        itemService.removeItem(cart, productId);
        return buildCartDTO(cartRepo.save(cart), productMap);
    }

    /** Empties the cart, e.g. after a successful checkout. Requires an existing cart. */
    public void clearCart(CartOwner owner) {
        emptyCart(requireCartEntity(owner));
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

    // ── Guest → account merge ─────────────────────────────────────

    /**
     * Merges the guest cart identified by the {@code suid} cookie into the signed-in user's
     * cart. Safe to call on every login: it is a no-op when no guest cart exists.
     */
    public void mergeGuestCartIntoAccount(UUID suid, User user) {
        if (suid == null || user == null) {
            return;
        }
        Cart guestCart = cartRepo.findBySuid(suid).orElse(null);
        if (guestCart == null || guestCart.getUser() != null) {
            return;
        }
        Cart accountCart = cartRepo.findByUserId(user.getId()).orElse(null);
        if (accountCart == null) {
            guestCart.setUser(user);
            cartRepo.save(guestCart);
            return;
        }
        if (Objects.equals(accountCart.getId(), guestCart.getId())) {
            return;
        }
        if (guestCart.getItems().isEmpty()) {
            cartRepo.delete(guestCart);
            return;
        }

        Set<Long> productIds = new HashSet<>();
        accountCart.getItems().forEach(item -> productIds.add(item.getProductId()));
        guestCart.getItems().forEach(item -> productIds.add(item.getProductId()));
        Map<Long, Product> productMap = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (CartItem guestItem : guestCart.getItems()) {
            int quantity = guestItem.getPriceSnapshot().getQuantity().intValue();
            try {
                itemService.addItem(accountCart, CreateItemDTO.builder()
                        .productId(guestItem.getProductId())
                        .quantity(quantity)
                        .build(), productMap);
            } catch (ResponseStatusException outOfStock) {
                // Keep whatever fits; never block a successful sign-in on a stock limit.
            }
        }
        cartRepo.save(accountCart);
        cartRepo.delete(guestCart);
    }

    // ── Internal ──────────────────────────────────────────────────

    /** Read-only DTO projection — does not purge or persist anything. */
    private CartDTO toCartDTO(Cart cart) {
        return buildCartDTO(cart, loadProductMap(cart));
    }

    /** DTO projection used by mutating flows; drops items whose product was deleted. */
    public CartDTO getCartDTO(Cart cart) {
        Map<Long, Product> productMap = loadProductMap(cart);
        if (itemService.purgeOrphans(cart, productMap)) {
            cartRepo.save(cart);
        }
        return buildCartDTO(cart, productMap);
    }

    // needs to be public (used by OrderService)
    public Map<Long, Product> loadProductMap(Cart cart) {
        Set<Long> ids = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
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

    private Map<Long, Product> loadProductMapForAdd(Cart cart, Long newProductId) {
        Set<Long> ids = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toCollection(HashSet::new));
        ids.add(newProductId);
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
            if (product == null) {
                continue;
            }
            int quantity = item.getPriceSnapshot().getQuantity().intValue();
            PriceSnapshot effective = priceService.refreshSnapshot(item.getPriceSnapshot(), () ->
                    product.getPrice().toBuilder()
                            .quantity(BigDecimal.valueOf(quantity))
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
}
