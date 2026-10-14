package com.example.ecommercedemo.repositories.products;

import com.example.ecommercedemo.entities.products.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /** Tracked products at or below the given stock level. */
    List<Product> findByStockLessThanEqual(Integer stock);

    /**
     * Atomically reduces tracked stock, but only while enough units remain.
     *
     * <p>The {@code stock >= :quantity} test runs inside the database, under the row lock, so
     * two concurrent checkouts cannot both win the last unit: the loser matches zero rows.
     * Untracked products ({@code stock IS NULL}) are never touched.</p>
     *
     * @return 1 when the decrement was applied, 0 when there was not enough stock
     */
    @Modifying(flushAutomatically = true)
    @Query("""
            update Product p
               set p.stock = p.stock - :quantity
             where p.id = :productId
               and p.stock is not null
               and p.stock >= :quantity
            """)
    int decrementStockIfAvailable(@Param("productId") Long productId, @Param("quantity") int quantity);
}
