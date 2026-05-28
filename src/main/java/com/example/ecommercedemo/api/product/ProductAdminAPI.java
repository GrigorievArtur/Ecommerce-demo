package com.example.ecommercedemo.api.product;

import com.example.ecommercedemo.dtos.products.CreateProductDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.dtos.products.UpdateProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/admin/products")
@Tag(name = "Admin Products CRUD")
public interface ProductAdminAPI {

    @Operation(
            summary = "Add product"
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ProductDTO> addProduct(@RequestBody CreateProductDTO createProductDTO);

    @Operation(
            summary = "Update product"
    )
    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ProductDTO> updateProduct(@RequestParam Long id, @RequestBody UpdateProductDTO updateProductDTO);
    @Operation(
            summary = "Delete product"
    )
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Void> deleteProduct(@RequestParam Long id);

}
