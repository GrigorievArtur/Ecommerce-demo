package com.example.ecommercedemo.repositories.shipping;

import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.entities.users.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingPresetsRepo extends JpaRepository<ShippingPreset, Long> {

    Page<ShippingPreset> findByUser(User user, Pageable pageable);
    Optional<ShippingPreset> findByIdAndUser(Long id, User user);
    int deleteByUserAndId(User user,Long id);
}