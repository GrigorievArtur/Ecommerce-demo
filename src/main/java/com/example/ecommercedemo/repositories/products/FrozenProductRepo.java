package com.example.ecommercedemo.repositories.products;

import com.example.ecommercedemo.entities.products.FrozenProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FrozenProductRepo extends JpaRepository<FrozenProduct, Long> {
}
