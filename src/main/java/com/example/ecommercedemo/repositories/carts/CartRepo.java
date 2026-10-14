package com.example.ecommercedemo.repositories.carts;

import com.example.ecommercedemo.entities.carts.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long user_id);

    Optional<Cart> findBySuid(UUID suid);

    /**
     * Locks the cart row for the rest of the checkout transaction. A second checkout of the
     * same cart waits here, then sees the emptied cart, so one cart produces one order.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.suid = :suid")
    Optional<Cart> findBySuidForUpdate(@Param("suid") UUID suid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.user.id = :userId")
    Optional<Cart> findByUserIdForUpdate(@Param("userId") Long userId);
}
