package com.example.ecommercedemo.entities.orders;

import com.example.ecommercedemo.entities.products.FrozenProduct;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.models.pricing.frozen.FrozenLinePrice;
import com.example.ecommercedemo.models.shipping.ShippingModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // May be null if a guest makes the order.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //TODO : Add not null validation.

    @Embedded
    private ShippingModel shippingModel;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<Long, FrozenLinePrice> items = new HashMap<>();


}
