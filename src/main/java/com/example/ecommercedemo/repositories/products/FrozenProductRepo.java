package com.example.ecommercedemo.repositories.products;

import com.example.ecommercedemo.entities.products.FrozenProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrozenProductRepo extends JpaRepository<FrozenProduct, Long> {
}
