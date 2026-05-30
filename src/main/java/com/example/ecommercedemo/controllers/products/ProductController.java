package com.example.ecommercedemo.controllers.products;

import com.example.ecommercedemo.api.products.ProductAPI;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.filters.products.ProductFilter;
import com.example.ecommercedemo.services.products.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Transactional
public class ProductController implements ProductAPI {

    private final ProductService productService;

    @Override
    public ResponseEntity<Page<ProductDTO>> getProductsPage(ProductFilter filter, Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(filter, pageable));
    }
}
