package com.example.ecommercedemo.services.products;

import com.example.ecommercedemo.dtos.products.CreateProductDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.dtos.products.UpdateProductDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.exceptions.ResourceNotFoundException;
import com.example.ecommercedemo.filters.products.ProductFilter;
import com.example.ecommercedemo.mappers.products.ProductMapper;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.specifications.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo productRepo;

    private final ProductMapper productMapper;


    public Page<ProductDTO> getAllProducts(ProductFilter filter, Pageable pageable) {
        return productRepo.findAll(ProductSpecification.withFilter(filter), pageable)
                .map(productMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return productMapper.toDTO(getProductEntity(id));
    }

    @Transactional(readOnly = true)
    public Product getProductEntity(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public long count() {
        return productRepo.count();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts(int threshold) {
        return productRepo.findByStockLessThanEqual(threshold).stream()
                .map(productMapper::toDTO)
                .toList();
    }

    /** Decrements tracked stock after a successful sale. No-op when stock is untracked. */
    public void decrementStock(Long productId, int quantity) {
        Product product = getProductEntity(productId);
        if (product.getStock() == null) {
            return;
        }
        product.setStock(Math.max(0, product.getStock() - quantity));
        productRepo.save(product);
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
