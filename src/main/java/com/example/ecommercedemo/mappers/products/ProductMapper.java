package com.example.ecommercedemo.mappers.products;

import com.example.ecommercedemo.dtos.products.CreateProductDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.dtos.products.UpdateProductDTO;
import com.example.ecommercedemo.entities.products.FrozenProduct;
import com.example.ecommercedemo.entities.products.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toDTO(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "price.grossPrice", source = "basePrice")
    @Mapping(target = "price.percentageDiscount", source = "discountPercentage")
    @Mapping(target = "price.grossAmount", ignore = true)
    @Mapping(target = "price.quantity", constant = "1")
    Product toEntity(CreateProductDTO productDTO);

    @Mapping(target = "id", ignore = true)
    FrozenProduct toFrozenProduct(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromDTO(UpdateProductDTO updateProductDTO, @MappingTarget Product product);
}
