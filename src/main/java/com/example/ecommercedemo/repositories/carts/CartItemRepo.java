package com.example.ecommercedemo.repositories.carts;

import com.example.ecommercedemo.entities.carts.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {
}
