package com.example.ecommercedemo.services.items;

import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.dtos.carts.items.ItemDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.mappers.products.ProductMapper;
import com.example.ecommercedemo.models.pricing.LinePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import com.example.ecommercedemo.models.pricing.frozen.FrozenLinePrice;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.pricing.PriceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public void addItem(Map<Long, FrozenLinePrice> items, CreateItemDTO createItemDTO) {
        Long productId = createItemDTO.getProductId();

        if (!items.containsKey(productId)) {
            FrozenLinePrice frozenLinePrice = refreshSnapshot(null, createItemDTO.getQuantity(), productId);
            items.put(productId, frozenLinePrice);
        } else {
            FrozenLinePrice existingPrice = items.get(productId);
            int newQuantity = existingPrice.getQuantity() + createItemDTO.getQuantity();

            FrozenLinePrice updatedPrice = refreshSnapshot(existingPrice, newQuantity, productId);
            items.put(productId, updatedPrice);
        }
    }

    public void decrementItem(Map<Long, FrozenLinePrice> items, Long productId, int quantity) {
        if (!items.containsKey(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart");
        }

        FrozenLinePrice existingPrice = items.get(productId);
        int remaining = existingPrice.getQuantity() - quantity;

        if (remaining <= 0) {
            items.remove(productId);
        } else {
            FrozenLinePrice updatedPrice = refreshSnapshot(null, remaining, productId);
            items.put(productId, updatedPrice);
        }
    }

    public void removeItem(Map<Long, FrozenLinePrice> items, Long productId) {
        if (!items.containsKey(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart");
        }
        items.remove(productId);
    }

    public ItemDTO toDTO(Long productId, FrozenLinePrice frozenLinePrice, Map<Long, Product> productMap) {
        Product product = productMap.get(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found for id: " + productId);
        }

        int currentQty = frozenLinePrice.getQuantity();
        FrozenLinePrice updatedPrice = priceService.refreshSnapshot(frozenLinePrice, () -> {
            return LinePrice.builder()
                    .quantity(currentQty)
                    .unitPrice(product.getUnitPrice())
                    .build();
        });

        // Mutate the reference mapping context to keep database snapshot updated
        frozenLinePrice.setFrozenUnitPrice(updatedPrice.getFrozenUnitPrice());

        return ItemDTO.builder()
                .product(productMapper.toDTO(product))
                .frozenLinePrice(frozenLinePrice)
                .build();
    }

    private FrozenLinePrice refreshSnapshot(FrozenLinePrice original, int quantity, Long productId) {
        return priceService.refreshSnapshot(original, () -> {
            UnitPrice liveUnitPrice = productRepo.findById(productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"))
                    .getUnitPrice();

            return LinePrice.builder()
                    .quantity(quantity)
                    .unitPrice(liveUnitPrice)
                    .build();
        });
    }
}