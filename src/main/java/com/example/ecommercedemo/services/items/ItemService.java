package com.example.ecommercedemo.services.items;

import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.dtos.carts.items.ItemDTO;
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
import java.util.Map;

@Service
@Transactional
public class ItemService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PriceService priceService;

    public void addItem(Map<Long, PriceSnapshot> items, CreateItemDTO createItemDTO) {
        Long productId = createItemDTO.getProductId();

        if (!items.containsKey(productId)) {
            PriceSnapshot snapshot = refreshSnapshot(null, createItemDTO.getQuantity(), productId);
            items.put(productId, snapshot);
        } else {
            PriceSnapshot existing = items.get(productId);
            int newQuantity = existing.getQuantity().intValue() + createItemDTO.getQuantity();

            PriceSnapshot updated = refreshSnapshot(existing, newQuantity, productId);
            items.put(productId, updated);
        }
    }

    public void decrementItem(Map<Long, PriceSnapshot> items, Long productId, int quantity) {
        if (!items.containsKey(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart");
        }

        PriceSnapshot existing = items.get(productId);
        int remaining = existing.getQuantity().intValue() - quantity;

        if (remaining <= 0) {
            items.remove(productId);
        } else {
            PriceSnapshot updated = refreshSnapshot(null, remaining, productId);
            items.put(productId, updated);
        }
    }

    public void removeItem(Map<Long, PriceSnapshot> items, Long productId) {
        if (!items.containsKey(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart");
        }
        items.remove(productId);
    }

    public ItemDTO toDTO(Long productId, PriceSnapshot snapshot, Map<Long, Product> productMap) {
        Product product = productMap.get(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found for id: " + productId);
        }

        int currentQty = snapshot.getQuantity().intValue();
        PriceSnapshot updated = priceService.refreshSnapshot(snapshot, () -> {
            Price livePrice = product.getPrice().toBuilder()
                    .quantity(BigDecimal.valueOf(currentQty))
                    .build();
            return livePrice;
        });

        // Mutate the reference to keep the in-memory map updated
        snapshot.setQuantity(updated.getQuantity());
        snapshot.setGrossPrice(updated.getGrossPrice());
        snapshot.setGrossAmount(updated.getGrossAmount());
        snapshot.setPercentageDiscount(updated.getPercentageDiscount());
        snapshot.setTimestamp(updated.getTimestamp());

        return ItemDTO.builder()
                .product(productMapper.toDTO(product))
                .priceSnapshot(snapshot)
                .build();
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
