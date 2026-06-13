package com.example.ecommercedemo.services.items;

import com.example.ecommercedemo.dtos.items.CreateItemDTO;
import com.example.ecommercedemo.dtos.items.ItemDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.mappers.items.ItemMapper;
import com.example.ecommercedemo.mappers.products.ProductMapper;
import com.example.ecommercedemo.models.common.PriceData;
import com.example.ecommercedemo.models.common.PriceSnapshot;
import com.example.ecommercedemo.models.items.ItemModel;
import com.example.ecommercedemo.models.pricing.LinePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import com.example.ecommercedemo.models.pricing.snapshots.SnapshotLinePrice;
import com.example.ecommercedemo.models.pricing.snapshots.SnapshotUnitPrice;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.pricing.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DurationFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
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

//        I am having hard time reading this shi 😭
        if (item == null) {
            ItemModel itemModel = itemMapper.toModel(createItemDTO);
            SnapshotLinePrice priceSnapshot = SnapshotLinePrice.from(
                    LinePrice.builder()
                            .quantity(createItemDTO.getQuantity())
                            .unitPrice(productRepo.findById(createItemDTO.getProductId())
                                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"))
                                    .getUnitPrice())
                            .build()
            );

            itemModel.setLinePrice(priceSnapshot);
            items.add(itemModel);

        } else {
            var linePrice = item.getLinePrice();
            linePrice.setQuantity(linePrice.getQuantity() + createItemDTO.getQuantity());
        }
    }

    public void decrementItem(List<ItemModel> items, Long productId, int quantity){
        var item = findCartItemModelInCart(items, productId);

        if (item == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not in cart");
        }
        int remaining = item.getLinePrice().getQuantity() - quantity;

        if (remaining <= 0) {
            items.remove(item);
        } else {
            item.getLinePrice().setQuantity(remaining);
        }

    }

    public void removeItem(List<ItemModel> items, Long productId) {
        var item = findCartItemModelInCart(items, productId);

        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart");
        }

        items.remove(item);
    }

    //Calculate prices
    //TODO: MAKE A NORMAL PRICE MODEL, MAYBE ANNOUNCE THE USER THAT HE USES A SNAPSHOT OR SHIT. I feel like it might a problem cuz plp have different prices
    public ItemDTO toDTO(ItemModel itemModel) {
        ItemDTO dto = itemMapper.toDTO(itemModel);

        ProductDTO productDTO = productMapper.toDTO(
                productRepo.findById(itemModel.getProductId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")));
        dto.setProduct(productDTO);

        SnapshotLinePrice linePrice = itemModel.getLinePrice();

        if (priceService.isSnapshotStale(linePrice)) {
            linePrice = SnapshotLinePrice.from(
                    LinePrice.builder()
                            .quantity(linePrice.getQuantity())
                            .unitPrice(productDTO.getPrice())
                            .build()
            );
            itemModel.setLinePrice(linePrice); // persist the refresh
        }

        dto.setUnitPrice(linePrice.getSnapshotUnitPrice().getSnapshotBasePrice().getBasePrice().getPrice());
        dto.setTotalPrice(linePrice.effectivePrice()); // line total, not unit price
        return dto;
    }

//    Some helper to check if it is unique in cart or not
    private ItemModel findCartItemModelInCart(List<ItemModel> items, Long productId) {
        return items.stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }





}
