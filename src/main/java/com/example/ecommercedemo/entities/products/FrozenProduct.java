package com.example.ecommercedemo.entities.products;
import com.example.ecommercedemo.entities.orders.Order;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.products.ProductMedia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FrozenProduct {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Size(min = 3, max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank
    @Size(min = 10, max = 5000)
    @Column(nullable = false, length = 5000)
    private String description;

    @Embedded
    private UnitPrice unitPrice;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @NotNull
    @Builder.Default
    private Instant snapshotTime = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Valid
    @Size(max = 20)
    @ElementCollection
    @CollectionTable(
            name = "product_media",
            joinColumns = @JoinColumn(name = "product_id")
    )
    private List<ProductMedia> mediaList;

}
