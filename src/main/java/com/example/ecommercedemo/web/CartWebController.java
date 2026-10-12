package com.example.ecommercedemo.web;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.services.carts.CartService;
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

import java.util.UUID;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartWebController {

    private final CartService cartService;

    @GetMapping
    public String view(@CookieValue(name = GlobalModelAttributes.CART_COOKIE, required = false) UUID suid,
                       @AuthenticationPrincipal User user,
                       Model model) {
        model.addAttribute("cart", cartService.getOrCreateCartDTO(suid, user));
        return "cart";
    }

    @PostMapping("/items")
    public String add(@RequestParam Long productId,
                      @RequestParam(defaultValue = "1") int quantity,
                      @CookieValue(name = GlobalModelAttributes.CART_COOKIE, required = false) UUID suid,
                      @AuthenticationPrincipal User user,
                      HttpServletResponse response,
                      RedirectAttributes redirect) {
        CartDTO cart = cartService.getOrCreateCartDTO(suid, user);
        GlobalModelAttributes.setCartCookie(response, cart.getSuid());
        try {
            cartService.addItemToCart(CreateItemDTO.builder()
                    .productId(productId)
                    .quantity(Math.max(1, quantity))
                    .build(), cart.getSuid());
            redirect.addFlashAttribute("flash", "Added to your cart.");
        } catch (ResponseStatusException ex) {
            redirect.addFlashAttribute("flashError", ex.getReason());
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}/decrement")
    public String decrement(@PathVariable Long productId,
                            @CookieValue(name = GlobalModelAttributes.CART_COOKIE, required = false) UUID suid,
                            @AuthenticationPrincipal User user,
                            HttpServletResponse response) {
        CartDTO cart = cartService.getOrCreateCartDTO(suid, user);
        GlobalModelAttributes.setCartCookie(response, cart.getSuid());
        cartService.decreaseItemFromCart(productId, 1, cart.getSuid());
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}/remove")
    public String remove(@PathVariable Long productId,
                         @CookieValue(name = GlobalModelAttributes.CART_COOKIE, required = false) UUID suid,
                         @AuthenticationPrincipal User user,
                         HttpServletResponse response) {
        CartDTO cart = cartService.getOrCreateCartDTO(suid, user);
        GlobalModelAttributes.setCartCookie(response, cart.getSuid());
        cartService.removeItemFromCart(productId, cart.getSuid());
        return "redirect:/cart";
    }
}
