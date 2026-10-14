package com.example.ecommercedemo.web;

import com.example.ecommercedemo.components.carts.CartCookie;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.models.carts.CartOwner;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.shipping.ShippingPolicy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Server-rendered cart. Reads never create a cart; only commands do, via
 * {@link CartService#addItem}.
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartWebController {

    private final CartService cartService;
    private final ShippingPolicy shippingPolicy;

    @GetMapping
    public String view(@CookieValue(name = CartCookie.NAME, required = false) UUID suid,
                       @AuthenticationPrincipal User user,
                       Model model) {
        CartOwner owner = CartOwner.of(user, suid);
        // No cart yet: show an empty one instead of creating a cart on a read.
        CartDTO cart = cartService.findCartDtoOrEmpty(owner).orElseGet(CartDTO::empty);
        model.addAttribute("cart", cart);
        addShippingAttributes(model, cart);
        return "cart";
    }

    @PostMapping("/items")
    public String add(@RequestParam Long productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      @CookieValue(name = CartCookie.NAME, required = false) UUID suid,
                      @AuthenticationPrincipal User user,
                      HttpServletResponse response,
                      RedirectAttributes redirect) {
        CartOwner owner = CartOwner.of(user, suid);
        try {
            CartDTO cart = cartService.addItem(owner, CreateItemDTO.builder()
                    .productId(productId)
                    .quantity(Math.max(1, quantity))
                    .build());
            CartCookie.write(response, cart.getSuid());
            redirect.addFlashAttribute("flash", "Added to your cart.");
        } catch (ResponseStatusException ex) {
            redirect.addFlashAttribute("flashError", ex.getReason());
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}/decrement")
    public String decrement(@PathVariable Long productId,
                            @CookieValue(name = CartCookie.NAME, required = false) UUID suid,
                            @AuthenticationPrincipal User user,
                            HttpServletResponse response,
                            RedirectAttributes redirect) {
        CartOwner owner = CartOwner.of(user, suid);
        try {
            CartDTO cart = cartService.decrementItem(owner, productId, 1);
            CartCookie.write(response, cart.getSuid());
        } catch (ResponseStatusException ex) {
            redirect.addFlashAttribute("flashError", ex.getReason());
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}/remove")
    public String remove(@PathVariable Long productId,
                         @CookieValue(name = CartCookie.NAME, required = false) UUID suid,
                         @AuthenticationPrincipal User user,
                         HttpServletResponse response,
                         RedirectAttributes redirect) {
        CartOwner owner = CartOwner.of(user, suid);
        try {
            CartDTO cart = cartService.removeItem(owner, productId);
            CartCookie.write(response, cart.getSuid());
            redirect.addFlashAttribute("flash", "Item removed from your cart.");
        } catch (ResponseStatusException ex) {
            redirect.addFlashAttribute("flashError", ex.getReason());
        }
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clear(@CookieValue(name = CartCookie.NAME, required = false) UUID suid,
                        @AuthenticationPrincipal User user,
                        RedirectAttributes redirect) {
        CartOwner owner = CartOwner.of(user, suid);
        try {
            cartService.clearCart(owner);
            redirect.addFlashAttribute("flash", "Your cart is now empty.");
        } catch (ResponseStatusException ex) {
            redirect.addFlashAttribute("flashError", ex.getReason());
        }
        return "redirect:/cart";
    }

    private void addShippingAttributes(Model model, CartDTO cart) {
        BigDecimal subtotal = cart.getPrice() == null || cart.getPrice().getGrossPrice() == null
                ? BigDecimal.ZERO
                : cart.getPrice().getGrossPrice();
        model.addAttribute("shippingFee", shippingPolicy.feeFor(subtotal));
        model.addAttribute("freeShippingThreshold", shippingPolicy.freeThreshold());
        model.addAttribute("amountUntilFreeShipping", shippingPolicy.amountUntilFreeDelivery(subtotal));
    }
}
