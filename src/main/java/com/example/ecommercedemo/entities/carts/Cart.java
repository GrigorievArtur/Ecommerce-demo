package com.example.ecommercedemo.entities.carts;

import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.models.carts.CartItemModel;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.mapstruct.Context;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID suid = UUID.randomUUID();

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false)
    private BigDecimal discountPercentage =  BigDecimal.ZERO;

    private Instant expiryDate;
    private Instant lastAccessDate;
    private Instant creationDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<CartItemModel> items = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();

        this.creationDate = now;
        this.lastAccessDate = now;

        if (this.user == null) {
            this.expiryDate = now.plus(1, ChronoUnit.DAYS);
        } else {
            this.expiryDate = null;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.lastAccessDate = Instant.now();

        if (this.user != null) {
            this.expiryDate = null;
        }
    }

}

