package com.example.ecommercedemo.services.products;

import com.example.ecommercedemo.dtos.products.CreateProductDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.dtos.products.UpdateProductDTO;
import com.example.ecommercedemo.exceptions.ResourceNotFoundException;
import com.example.ecommercedemo.filters.products.ProductFilter;
import com.example.ecommercedemo.mappers.products.ProductMapper;
import com.example.ecommercedemo.models.carts.ItemModel;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.specifications.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo productRepo;

    private final ProductMapper productMapper;

    public ProductDTO itemToProductDTO(ItemModel itemModel) {
        var dto = productRepo.findById(itemModel.getProductId()).orElseThrow();
        return productMapper.toDTO(dto);
    }

    public Page<ProductDTO> getAllProducts(ProductFilter filter, Pageable pageable) {
        return productRepo.findAll(ProductSpecification.withFilter(filter), pageable)
                .map(productMapper::toDTO);
    }

    public void deleteProductById(Long id) {
        var product = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        productRepo.delete(product);
    }

    public ProductDTO updateProductById(Long id, UpdateProductDTO productDTO) {
        var original = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        productMapper.updateProductFromDTO(productDTO, original);
        var updated = productRepo.save(original);
        return productMapper.toDTO(updated);
    }

    public ProductDTO createProduct(CreateProductDTO createProductDTO) {
        var product = productMapper.toEntity(createProductDTO);
        return productMapper.toDTO(productRepo.save(product));
    }



}
