package com.example.ecommercedemo.api.products;

import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.filters.products.ProductFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("api/products")
@Tag(name = "Products")
public interface ProductAPI {

    @GetMapping
    @Operation(
            summary = "Gets page of products",
            description = "Gets a page of products based on entered filters"
    )
    ResponseEntity<Page<ProductDTO>> getProductsPage(@ModelAttribute ProductFilter filter, Pageable pageable);

}
