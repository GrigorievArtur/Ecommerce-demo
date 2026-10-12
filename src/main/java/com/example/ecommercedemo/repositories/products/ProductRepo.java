package com.example.ecommercedemo.repositories.products;

import com.example.ecommercedemo.entities.products.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /** Tracked products at or below the given stock level. */
    List<Product> findByStockLessThanEqual(Integer stock);
}
