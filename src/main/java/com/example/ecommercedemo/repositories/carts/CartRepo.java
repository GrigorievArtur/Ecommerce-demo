package com.example.ecommercedemo.repositories.carts;

import com.example.ecommercedemo.entities.carts.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser_Id(Long user_id);

    Optional<Cart> findBySuid(UUID suid);
}
