package com.example.ecommercedemo.services.orders;

import com.example.ecommercedemo.dtos.orders.OrderDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.carts.CartItem;
import com.example.ecommercedemo.entities.orders.OrderItem;
import com.example.ecommercedemo.entities.orders.OrderSnapshot;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.mappers.orders.OrderMapper;
import com.example.ecommercedemo.mappers.shipping.ShippingMapper;
import com.example.ecommercedemo.repositories.orders.OrderRepo;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.items.ItemService;
import com.example.ecommercedemo.services.shipping.ShippingPresetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private OrderRepo orderRepo;

    public OrderSnapshot createOrder(Cart cart, User user) {
        OrderSnapshot orderSnapshot = orderMapper.toOrderSnapshot(cart);
        Map<Long, Product> productMap = cartService.loadProductMap(cart);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .product(productMap.get(cartItem.getProductId()))
                    .orderSnapshot(orderSnapshot)
                    .priceSnapshot(cartItem.getPriceSnapshot())
                    .build();
            orderItems.add(orderItem);
        }
        orderSnapshot.setUser(user);
        orderSnapshot.setItems(orderItems);

        return orderRepo.save(orderSnapshot);
    }

    public OrderDTO toOrderDTO(OrderSnapshot orderSnapshot) {
        return OrderDTO.builder()
                .items(orderSnapshot.getItems().stream().map(itemService::toDTO).collect(Collectors.toList()))
                .shippingPreset(shippingMapper.toDTO(orderSnapshot.getShippingPreset()))
                .userId(orderSnapshot.getUser().getId())
                .id(orderSnapshot.getId())
                .build();
    }
}
