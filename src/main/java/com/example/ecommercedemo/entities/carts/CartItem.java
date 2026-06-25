package com.example.ecommercedemo.entities.carts;

import com.example.ecommercedemo.models.pricing.PriceSnapshot;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    /** The product this item references (was the Map key) */
    @Column(nullable = false)
    private Long productId;

    /** Snapshot of price + quantity at time item was added/updated */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private PriceSnapshot priceSnapshot;

}
