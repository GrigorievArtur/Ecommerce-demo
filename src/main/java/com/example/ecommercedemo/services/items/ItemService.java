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
            PriceSnapshot snapshot = snapshotFromProduct(product, dto.getQuantity());
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productId(productId)
                    .priceSnapshot(snapshot)
                    .build();
            items.add(item);
        } else {
            CartItem item = existing.get();
            int newQty = item.getPriceSnapshot().getQuantity().intValue() + dto.getQuantity();
            item.setPriceSnapshot(refreshSnapshotFromProduct(item.getPriceSnapshot(), product, newQty));
        }
    }

    public void decrementItem(Cart cart, Long productId, int quantity, Map<Long, Product> productMap) {
        CartItem item = findByProductId(cart, productId);
        Product product = productMap.get(productId);

        int remaining = item.getPriceSnapshot().getQuantity().intValue() - quantity;
        if (remaining <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setPriceSnapshot(refreshSnapshotFromProduct(null, product, remaining));
        }
    }

    public void removeItem(Cart cart, Long productId) {
        CartItem item = findByProductId(cart, productId);
        cart.getItems().remove(item);
    }

    public ItemDTO toDTO(CartItem item, Map<Long, Product> productMap) {
        Long productId = item.getProductId();
        Product product = productMap.get(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found for id: " + productId);
        }

        PriceSnapshot snapshot = item.getPriceSnapshot();
        int currentQty = snapshot.getQuantity().intValue();

        PriceSnapshot effective = priceService.refreshSnapshot(snapshot, () ->
                product.getPrice().toBuilder()
                        .quantity(BigDecimal.valueOf(currentQty))
                        .build()
        );

        return ItemDTO.builder()
                .product(productMapper.toDTO(product))
                .priceSnapshot(effective)
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
        Price live = product.getPrice().toBuilder()
                .quantity(BigDecimal.valueOf(dto.getQuantity()))
                .build();
        return CartItem.builder()
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

    private PriceSnapshot refreshSnapshotFromProduct(PriceSnapshot original, Product product, int quantity) {
        return priceService.refreshSnapshot(original, () ->
                product.getPrice().toBuilder()
                        .quantity(BigDecimal.valueOf(quantity))
                        .build()
        );
    }
}
