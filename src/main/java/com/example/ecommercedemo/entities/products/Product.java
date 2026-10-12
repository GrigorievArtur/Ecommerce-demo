package com.example.ecommercedemo.entities.products;
import com.example.ecommercedemo.models.pricing.Price;
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
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Product {
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
    @Builder.Default
    private Price price = new Price();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    /**
     * Units available for sale. {@code null} means unlimited / not tracked.
     */
    @Min(0)
    @Column(name = "stock")
    private Integer stock;

    @Valid
    @Size(max = 20)
    @ElementCollection
    @CollectionTable(
            name = "product_media",
            joinColumns = @JoinColumn(name = "product_id")
    )
    private List<ProductMedia> mediaList;

    /** Guards against legacy rows whose embedded price columns are all NULL. */
    @PostLoad
    void hydratePrice() {
        if (price == null) {
            price = new Price();
        }
    }

    @PrePersist
    @PreUpdate
    void ensurePrice() {
        if (price == null) {
            price = new Price();
        }
    }
}
