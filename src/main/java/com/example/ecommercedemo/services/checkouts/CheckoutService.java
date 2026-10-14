package com.example.ecommercedemo.services.checkouts;

import com.example.ecommercedemo.dtos.orders.OrderDTO;
import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.orders.OrderItem;
import com.example.ecommercedemo.entities.orders.OrderSnapshot;
import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.models.carts.CartOwner;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.orders.OrderService;
import com.example.ecommercedemo.services.products.ProductService;
import com.example.ecommercedemo.services.shipping.ShippingPresetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Turns a cart into a persisted order.
 *
 * <p>The owner is passed in explicitly ({@link CartOwner}) so the checkout flow never has
 * to infer guest vs signed-in identity. The whole side effect — order creation, stock
 * decrement and cart clearing — stays in one transaction so the REST API and the
 * server-rendered checkout behave identically.</p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final OrderService orderService;
    private final ShippingPresetService shippingPresetService;
    private final ProductService productService;

    public OrderDTO checkoutDefault(CartOwner owner) {
        ShippingPreset shippingPreset = shippingPresetService.getDefaultShippingPresetEntity(accountOf(owner));
        return orderService.toOrderDTO(checkout(owner, shippingPreset));
    }

    public OrderDTO checkoutSaved(CartOwner owner, Long shippingPresetId) {
        ShippingPreset shippingPreset = shippingPresetService.getShippingPresetEntity(shippingPresetId, accountOf(owner));
        return orderService.toOrderDTO(checkout(owner, shippingPreset));
    }

    public OrderDTO checkoutNew(CartOwner owner, CreateShippingPresetDTO createShippingPresetDTO) {
        return checkoutNew(owner, createShippingPresetDTO, true);
    }

    /**
     * @param savePreset when {@code false} the address is attached to the order only and is
     *                   not stored as a reusable preset — even for a signed-in customer.
     */
    public OrderDTO checkoutNew(CartOwner owner, CreateShippingPresetDTO createShippingPresetDTO, boolean savePreset) {
        User user = accountOf(owner);
        ShippingPreset shippingPreset = (user != null && savePreset)
                ? shippingPresetService.saveShippingPresetEntity(createShippingPresetDTO, user)
                : shippingPresetService.toEntity(createShippingPresetDTO);
        return orderService.toOrderDTO(checkout(owner, shippingPreset));
    }

    public OrderSnapshot checkout(CartOwner owner, ShippingPreset shippingPreset) {
        Cart cart = cartService.requireCartEntity(owner);
        cartService.assertItemsInStock(cart);

        OrderSnapshot order = orderService.createOrder(cart, accountOf(owner));
        // The order is a managed entity, so this flush-on-commit persists the JSON address.
        order.setShippingPreset(shippingPreset);

        decrementStock(order);
        cartService.emptyCart(cart);
        return order;
    }

    /** {@code null} for a guest owner. */
    private User accountOf(CartOwner owner) {
        return owner instanceof CartOwner.Account account ? account.user() : null;
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
