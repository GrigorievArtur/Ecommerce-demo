package com.example.ecommercedemo.services.items;

import com.example.ecommercedemo.dtos.items.CreateItemDTO;
import com.example.ecommercedemo.dtos.items.ItemDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.mappers.items.ItemMapper;
import com.example.ecommercedemo.mappers.products.ProductMapper;
import com.example.ecommercedemo.models.items.ItemModel;
import com.example.ecommercedemo.models.pricing.LinePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import com.example.ecommercedemo.models.pricing.frozen.FrozenLinePrice;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.pricing.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private PriceService priceService;

    public void addItem(List<ItemModel> items, CreateItemDTO createItemDTO) {
        var item = findCartItemModelInCart(items, createItemDTO.getProductId());

        if (item == null) {
            ItemModel itemModel = itemMapper.toModel(createItemDTO);

            // Using refreshSnapshot with a null initial state forces standard initial creation
            // inside your pricing engine via clean lambda structure, uhhh 🥲
            FrozenLinePrice frozenLinePrice = priceService.refreshSnapshot(null, () -> {
                UnitPrice liveUnitPrice = productRepo.findById(createItemDTO.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"))
                        .getUnitPrice();

                return LinePrice.builder()
                        .quantity(createItemDTO.getQuantity())
                        .unitPrice(liveUnitPrice)
                        .build();
            });

            itemModel.setFrozenLinePrice(frozenLinePrice);
            items.add(itemModel);
        } else {
            var linePrice = item.getFrozenLinePrice();
            linePrice.setQuantity(linePrice.getQuantity() + createItemDTO.getQuantity());
        }
    }

    public void decrementItem(List<ItemModel> items, Long productId, int quantity) {
        var item = findCartItemModelInCart(items, productId);

        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart");
        }

        int remaining = item.getFrozenLinePrice().getQuantity() - quantity;

        if (remaining <= 0) {
            items.remove(item);
        } else {
            item.getFrozenLinePrice().setQuantity(remaining);
        }
    }

    public void removeItem(List<ItemModel> items, Long productId) {
        var item = findCartItemModelInCart(items, productId);

        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart");
        }

        items.remove(item);
    }

    public ItemDTO toDTO(ItemModel itemModel) {
        ItemDTO dto = itemMapper.toDTO(itemModel);

        ProductDTO productDTO = productMapper.toDTO(
                productRepo.findById(itemModel.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"))
        );
        dto.setProduct(productDTO);

        FrozenLinePrice frozenLinePrice = itemModel.getFrozenLinePrice();

        int currentQty = (frozenLinePrice != null) ? frozenLinePrice.getQuantity() : 1;
        frozenLinePrice = priceService.refreshSnapshot(frozenLinePrice, () -> {
            UnitPrice liveProductPrice = productRepo.findById(itemModel.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"))
                    .getUnitPrice();

            return LinePrice.builder()
                    .quantity(currentQty)
                    .unitPrice(liveProductPrice)
                    .build();
        });

        // Persist updated snapshot back into your structural reference layout context
        itemModel.setFrozenLinePrice(frozenLinePrice);

        dto.setUnitPrice(frozenLinePrice.getFrozenUnitPrice().getFrozenPrice().effectivePrice());
        dto.setTotalPrice(frozenLinePrice.effectivePrice());

        return dto;
    }

    private ItemModel findCartItemModelInCart(List<ItemModel> items, Long productId) {
        return items.stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }
}