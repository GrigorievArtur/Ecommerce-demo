package com.example.ecommercedemo.entities.orders;

import com.example.ecommercedemo.entities.products.FrozenProduct;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.models.shipping.ShippingModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
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

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FrozenProduct> products = new ArrayList<>();




}
