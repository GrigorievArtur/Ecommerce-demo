package com.example.ecommercedemo.services.items;

import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.dtos.carts.items.ItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.carts.CartItem;
import com.example.ecommercedemo.entities.orders.OrderItem;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.mappers.products.ProductMapper;
import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.models.pricing.PriceSnapshot;
import com.example.ecommercedemo.services.pricing.PriceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ProductMapper productMapper;
    private final PriceService priceService;

    public void addItem(Cart cart, CreateItemDTO dto, Map<Long, Product> productMap) {
        Long productId = dto.getProductId();
        Product product = productMap.get(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId);
        }
        List<CartItem> items = cart.getItems();

        Optional<CartItem> existing = items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (existing.isEmpty()) {
            assertStock(product, dto.getQuantity());
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productId(productId)
                    .priceSnapshot(snapshotFromProduct(product, dto.getQuantity()))
                    .build();
            items.add(item);
        } else {
            CartItem item = existing.get();
            int newQuantity = item.getPriceSnapshot().getQuantity().intValue() + dto.getQuantity();
            assertStock(product, newQuantity);
            item.setPriceSnapshot(refreshSnapshotFromProduct(product, newQuantity));
        }
    }

    public void decrementItem(Cart cart, Long productId, int quantity, Map<Long, Product> productMap) {
        CartItem item = findByProductId(cart, productId);
        Product product = productMap.get(productId);

        int remaining = item.getPriceSnapshot().getQuantity().intValue() - quantity;
        if (remaining <= 0) {
            cart.getItems().remove(item);
        } else if (product != null) {
            item.setPriceSnapshot(refreshSnapshotFromProduct(product, remaining));
        }
    }

    public void removeItem(Cart cart, Long productId) {
        cart.getItems().remove(findByProductId(cart, productId));
    }

    /** Returns null if the product was deleted — the caller purges null entries. */
    public ItemDTO toDTO(CartItem item, Map<Long, Product> productMap) {
        Long productId = item.getProductId();
        Product product = productMap.get(productId);
        if (product == null) {
            return null;
        }
        int currentQuantity = item.getPriceSnapshot().getQuantity().intValue();
        PriceSnapshot effective = priceService.refreshSnapshot(item.getPriceSnapshot(), () ->
                product.getPrice().toBuilder()
                        .quantity(BigDecimal.valueOf(currentQuantity))
                        .build()
        );
        return ItemDTO.builder()
                .product(productMapper.toDTO(product))
                .priceSnapshot(effective)
                .build();
    }

    public ItemDTO toDTO(OrderItem item) {
        return ItemDTO.builder()
                .product(productMapper.toDTO(item.getProduct()))
                .priceSnapshot(item.getPriceSnapshot())
                .build();
    }

    public PriceSnapshot snapshotFromProduct(Product product, int quantity) {
        Price live = product.getPrice().toBuilder()
                .quantity(BigDecimal.valueOf(quantity))
                .build();
        return priceService.snapshotFrom(live);
    }

    public CartItem toCartItem(CreateItemDTO dto, Map<Long, Product> productMap) {
        Product product = productMap.get(dto.getProductId());
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + dto.getProductId());
        }
        assertStock(product, dto.getQuantity());
        return CartItem.builder()
                .productId(dto.getProductId())
                .priceSnapshot(snapshotFromProduct(product, dto.getQuantity()))
                .build();
    }

    /** Removes cart items whose products no longer exist. Returns true if any were purged. */
    public boolean purgeOrphans(Cart cart, Map<Long, Product> productMap) {
        return cart.getItems().removeIf(item -> !productMap.containsKey(item.getProductId()));
    }

    // --- helpers ---

    /** Throws 409 when more units are requested than are tracked in stock. */
    private void assertStock(Product product, int requested) {
        if (product.getStock() != null && requested > product.getStock()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only " + product.getStock() + " left in stock for " + product.getName());
        }
    }

    private CartItem findByProductId(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart"));
    }

    private PriceSnapshot refreshSnapshotFromProduct(Product product, int quantity) {
        return priceService.refreshSnapshot(null, () ->
                product.getPrice().toBuilder()
                        .quantity(BigDecimal.valueOf(quantity))
                        .build()
        );
    }
}
