package com.example.ecommercedemo.entities.orders;

import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.products.ProductMedia;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long productId;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String productName;

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String productDescription;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category productCategory;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Valid
    @Size(max = 20)
    @ElementCollection
    @CollectionTable(
            name = "order_item_media",
            joinColumns = @JoinColumn(name = "order_item_id")
    )
    private List<ProductMedia> mediaList;

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}




