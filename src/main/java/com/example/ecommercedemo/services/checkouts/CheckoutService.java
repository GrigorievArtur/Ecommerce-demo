package com.example.ecommercedemo.services.checkouts;

import com.example.ecommercedemo.dtos.orders.OrderDTO;
import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.orders.OrderItem;
import com.example.ecommercedemo.entities.orders.OrderSnapshot;
import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.orders.OrderService;
import com.example.ecommercedemo.services.products.ProductService;
import com.example.ecommercedemo.services.shipping.ShippingPresetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Turns the current cart into a persisted order.
 *
 * <p>Keeping the whole side effect — order creation, stock decrement and cart clearing —
 * in one transactional method means the REST API and the server-rendered checkout behave
 * identically and can never leave a charged customer with a non-empty cart.</p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final OrderService orderService;
    private final ShippingPresetService shippingPresetService;
    private final ProductService productService;

    public OrderDTO checkoutDefault(UUID suid, User user) {
        ShippingPreset shippingPreset = shippingPresetService.getDefaultShippingPresetEntity(user);
        return orderService.toOrderDTO(checkout(suid, user, shippingPreset));
    }

    public OrderDTO checkoutSaved(UUID suid, User user, Long shippingPresetId) {
        ShippingPreset shippingPreset = shippingPresetService.getShippingPresetEntity(shippingPresetId, user);
        return orderService.toOrderDTO(checkout(suid, user, shippingPreset));
    }

    public OrderDTO checkoutNew(UUID suid, User user, CreateShippingPresetDTO createShippingPresetDTO) {
        return checkoutNew(suid, user, createShippingPresetDTO, true);
    }

    /**
     * @param savePreset when {@code false} the address is attached to the order only and is
     *                   not stored as a reusable preset — even for a signed-in customer.
     */
    public OrderDTO checkoutNew(UUID suid, User user, CreateShippingPresetDTO createShippingPresetDTO, boolean savePreset) {
        ShippingPreset shippingPreset = (user != null && savePreset)
                ? shippingPresetService.saveShippingPresetEntity(createShippingPresetDTO, user)
                : shippingPresetService.toEntity(createShippingPresetDTO);
        return orderService.toOrderDTO(checkout(suid, user, shippingPreset));
    }

    public OrderSnapshot checkout(UUID suid, User user, ShippingPreset shippingPreset) {
        Cart cart = cartService.resolveCart(suid);
        cartService.assertItemsInStock(cart);

        OrderSnapshot order = orderService.createOrder(cart, user);
        // The order is a managed entity, so this flush-on-commit persists the JSON address.
        order.setShippingPreset(shippingPreset);

        decrementStock(order);
        cartService.emptyCart(cart);
        return order;
    }

    /** Reduces tracked stock for every line, so the same sale can never oversell. */
    private void decrementStock(OrderSnapshot order) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() == null || item.getProduct().getId() == null) {
                continue;
            }
            int quantity = item.getPriceSnapshot().getQuantity().intValue();
            productService.decrementStock(item.getProduct().getId(), quantity);
        }
    }
}
