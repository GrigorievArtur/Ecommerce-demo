package com.example.ecommercedemo.repositories.orders;

import com.example.ecommercedemo.entities.orders.OrderSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<OrderSnapshot, Long> {
    List<OrderSnapshot> findByUserIdOrderByIdDesc(Long userId);
}
