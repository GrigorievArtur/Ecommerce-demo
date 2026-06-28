package com.example.ecommercedemo.entities.carts;

import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.models.pricing.Price;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    @Builder.Default
    @Column(nullable = false, unique = true, updatable = false)
    private UUID suid = UUID.randomUUID();

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Price price = new Price();

    private Instant expiryDate;
    private Instant lastAccessDate;
    private Instant creationDate;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cart cart)) return false;
        return Objects.equals(id, cart.id) && Objects.equals(suid, cart.suid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, suid);
    }

}
