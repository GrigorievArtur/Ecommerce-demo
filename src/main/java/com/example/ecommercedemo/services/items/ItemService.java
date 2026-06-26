package com.example.ecommercedemo.services.items;

import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.dtos.carts.items.ItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.carts.CartItem;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.mappers.products.ProductMapper;
import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.models.pricing.PriceSnapshot;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.pricing.PriceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ItemService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PriceService priceService;

    public void addItem(Cart cart, CreateItemDTO createItemDTO) {
        Long productId = createItemDTO.getProductId();
        List<CartItem> items = cart.getItems();

        Optional<CartItem> existing = items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (existing.isEmpty()) {
            PriceSnapshot snapshot = refreshSnapshot(null, createItemDTO.getQuantity(), productId);
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productId(productId)
                    .priceSnapshot(snapshot)
                    .build();
            items.add(item);
        } else {
            CartItem item = existing.get();
            int currentQty = item.getPriceSnapshot().getQuantity().intValue();
            int newQty = currentQty + createItemDTO.getQuantity();
            PriceSnapshot updated = refreshSnapshot(item.getPriceSnapshot(), newQty, productId);
            item.setPriceSnapshot(updated);
        }
    }

    public void decrementItem(Cart cart, Long productId, int quantity) {
        CartItem item = findByProductId(cart, productId);

        int remaining = item.getPriceSnapshot().getQuantity().intValue() - quantity;

        if (remaining <= 0) {
            cart.getItems().remove(item);
            item.setCart(null); // break reference for orphan removal
        } else {
            PriceSnapshot updated = refreshSnapshot(null, remaining, productId);
            item.setPriceSnapshot(updated);
        }
    }

    public void removeItem(Cart cart, Long productId) {
        CartItem item = findByProductId(cart, productId);
        cart.getItems().remove(item);
        item.setCart(null);
    }

    public ItemDTO toDTO(CartItem item, Map<Long, Product> productMap) {
        Long productId = item.getProductId();
        Product product = productMap.get(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found for id: " + productId);
        }

        PriceSnapshot snapshot = item.getPriceSnapshot();
        int currentQty = snapshot.getQuantity().intValue();

        PriceSnapshot updated = priceService.refreshSnapshot(snapshot, () ->
                product.getPrice().toBuilder()
                        .quantity(BigDecimal.valueOf(currentQty))
                        .build()
        );

        item.setPriceSnapshot(updated);

        return ItemDTO.builder()
                .product(productMapper.toDTO(product))
                .priceSnapshot(item.getPriceSnapshot())
                .build();
    }

    public CartItem toCartItem(CreateItemDTO dto, Cart cart, Map<Long, Product> productMap) {
        Product product = productMap.get(dto.getProductId());
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + dto.getProductId());
        }
        Price live = product.getPrice().toBuilder()
                .quantity(BigDecimal.valueOf(dto.getQuantity()))
                .build();
        return CartItem.builder()
                .cart(cart)
                .productId(dto.getProductId())
                .priceSnapshot(priceService.snapshotFrom(live))
                .build();
    }

    // --- helpers ---

    private CartItem findByProductId(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart"));
    }

    private PriceSnapshot refreshSnapshot(PriceSnapshot original, int quantity, Long productId) {
        return priceService.refreshSnapshot(original, () -> {
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
            return product.getPrice().toBuilder()
                    .quantity(BigDecimal.valueOf(quantity))
                    .build();
        });
    }
}
