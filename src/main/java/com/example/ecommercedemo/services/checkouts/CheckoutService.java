package com.example.ecommercedemo.services.checkouts;

import com.example.ecommercedemo.dtos.orders.OrderDTO;
import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.orders.OrderSnapshot;
import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.orders.OrderService;
import com.example.ecommercedemo.services.shipping.ShippingPresetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CheckoutService {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShippingPresetService shippingPresetService;

    public OrderDTO checkoutDefault(UUID suid, User user) {
        ShippingPreset shippingPreset = shippingPresetService.getDefaultShippingPresetEntity(user);
        return orderService.toOrderDTO(checkout(suid, user, shippingPreset));
    }

    public OrderDTO checkoutSaved(UUID suid, User user, Long ShippingPresetId) {
        ShippingPreset shippingPreset = shippingPresetService.getShippingPresetEntity(ShippingPresetId, user);
        return orderService.toOrderDTO(checkout(suid, user, shippingPreset));
    }

    public OrderDTO checkoutNew(UUID suid, User user, CreateShippingPresetDTO createShippingPresetDTO) {
        ShippingPreset shippingPreset = shippingPresetService.saveShippingPresetEntity(createShippingPresetDTO, user);
        return orderService.toOrderDTO(checkout(suid, user, shippingPreset));
    }

    public OrderSnapshot checkout(UUID suid, User user, ShippingPreset shippingPreset) {
        Cart cart = cartService.resolveCart(suid);
        OrderSnapshot order = orderService.createOrder(cart, user);
        order.setShippingPreset(shippingPreset);
        return order;
    }
}
