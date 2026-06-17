package com.example.ecommercedemo.services;

import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.orders.OrderSnapshot;
import com.example.ecommercedemo.mappers.orders.OrderMapper;
import com.example.ecommercedemo.services.carts.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderMapper orderMapper;

    public OrderSnapshot createOrder(Cart cart) {
        OrderSnapshot orderSnapshot = orderMapper.toOrderSnapshot(cart);
        //TODO : IMPLEMENT THIS FUNCTION COMPLETLY.
        return orderSnapshot;

    }

}
