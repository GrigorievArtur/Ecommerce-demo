package com.example.ecommercedemo.services.items;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.items.CreateItemDTO;
import com.example.ecommercedemo.dtos.items.ItemDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.mappers.items.ItemMapper;
import com.example.ecommercedemo.mappers.carts.CartMapper;
import com.example.ecommercedemo.mappers.products.ProductMapper;
import com.example.ecommercedemo.models.carts.ItemModel;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.carts.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ItemService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ItemMapper itemMapper;



    public void addItem(List<ItemModel> items, CreateItemDTO createItemDTO) {
        var item = findCartItemModelInCart(items, createItemDTO.getProductId());

        if (item == null) {
            items.add(itemMapper.toModel(createItemDTO));
        } else {
            item.setQuantity(item.getQuantity() + createItemDTO.getQuantity());
        }
    }

    public void decrementItem(List<ItemModel> items, Long productId, int quantity){
        var item = findCartItemModelInCart(items, productId);

        if (item == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not in cart");
        }
        int remaining = item.getQuantity() - quantity;

        if (remaining <= 0) {
            items.remove(item);
        } else {
            item.setQuantity(remaining);
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
    public ItemDTO toDTO(ItemModel itemModel) {
        ItemDTO dto = itemMapper.toDTO(itemModel);

        ProductDTO productDTO = productMapper.toDTO(productRepo.findById(itemModel.getProductId()).orElseThrow());
        dto.setProduct(productDTO);

        BigDecimal totalPrice = productDTO.getSalePrice().multiply(BigDecimal.valueOf(dto.getQuantity()));

        dto.setBasePrice(productDTO.getSalePrice());
        dto.setTotalPrice(totalPrice);

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
