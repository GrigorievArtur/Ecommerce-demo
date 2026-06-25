package com.example.ecommercedemo.controllers.products;

import com.example.ecommercedemo.api.products.ProductAdminAPI;
import com.example.ecommercedemo.dtos.products.CreateProductDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.dtos.products.UpdateProductDTO;
import com.example.ecommercedemo.services.products.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
@RequestMapping("api/admin/products")
public class ProductAdminApiImpl implements ProductAdminAPI {

    @Autowired
    private ProductService productService;

    @Override
    public ResponseEntity<ProductDTO> addProduct(CreateProductDTO createProductDTO) {
        return new ResponseEntity<>(productService.createProduct(createProductDTO), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ProductDTO> updateProduct(Long id, UpdateProductDTO updateProductDTO) {
        return new ResponseEntity<>(productService.updateProductById(id, updateProductDTO), HttpStatus.OK);
    }

    // api/admin/products?id=2
    // api/admin/products/2

    @Override
    public ResponseEntity<Void> deleteProduct(Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }
}
