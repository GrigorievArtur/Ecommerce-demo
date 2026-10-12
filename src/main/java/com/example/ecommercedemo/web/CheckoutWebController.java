package com.example.ecommercedemo.web;

import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.orders.OrderDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.payments.PaymentGateway;
import com.example.ecommercedemo.payments.PaymentRequest;
import com.example.ecommercedemo.payments.PaymentResult;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.checkouts.CheckoutService;
import com.example.ecommercedemo.services.pricing.PriceService;
import com.example.ecommercedemo.services.products.ProductService;
import com.example.ecommercedemo.web.forms.CheckoutForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutWebController {

    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final ProductService productService;
    private final PaymentGateway paymentGateway;

    @GetMapping
    public String form(@CookieValue(name = GlobalModelAttributes.CART_COOKIE, required = false) UUID suid,
                       @AuthenticationPrincipal User user,
                       Model model) {
        CartDTO cart = cartService.getOrCreateCartDTO(suid, user);
        model.addAttribute("cart", cart);
        model.addAttribute("total", cartTotal(cart));
        if (!model.containsAttribute("checkoutForm")) {
            model.addAttribute("checkoutForm", new CheckoutForm());
        }
        model.addAttribute("paymentProvider", paymentGateway.providerName());
        return "checkout";
    }

    @PostMapping
    public String place(@Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                        BindingResult binding,
                        @CookieValue(name = GlobalModelAttributes.CART_COOKIE, required = false) UUID suid,
                        @AuthenticationPrincipal User user,
                        Model model,
                        RedirectAttributes redirect) {

        CartDTO cart = cartService.getOrCreateCartDTO(suid, user);
        BigDecimal total = cartTotal(cart);

        if (cart.getItems().isEmpty()) {
            redirect.addFlashAttribute("flashError", "Your cart is empty.");
            return "redirect:/cart";
        }
        if (binding.hasErrors()) {
            model.addAttribute("cart", cart);
            model.addAttribute("total", total);
            model.addAttribute("fieldErrors", WebErrors.fieldErrors(binding));
            model.addAttribute("paymentProvider", paymentGateway.providerName());
            return "checkout";
        }

        PaymentResult payment = paymentGateway.charge(new PaymentRequest(
                total,
                "USD",
                form.getMethod(),
                form.getCardNumber(),
                form.getCardHolder(),
                form.getExpiry(),
                form.getCvc()));

        if (!payment.success()) {
            model.addAttribute("cart", cart);
            model.addAttribute("total", total);
            model.addAttribute("paymentError", payment.message());
            model.addAttribute("paymentProvider", paymentGateway.providerName());
            return "checkout";
        }

        // Payment approved — persist the order, then drain stock and the cart.
        OrderDTO order = checkoutService.checkoutNew(suid, user, form.toShippingPreset());
        order.getItems().forEach(item -> productService.decrementStock(
                item.getProduct().getId(),
                item.getPriceSnapshot().getQuantity().intValue()));
        cartService.clearCart(suid);

        redirect.addFlashAttribute("order", order);
        redirect.addFlashAttribute("transactionId", payment.transactionId());
        return "redirect:/checkout/confirmation";
    }

    @GetMapping("/confirmation")
    public String confirmation(@ModelAttribute(value = "order", binding = false) OrderDTO order) {
        return order == null ? "redirect:/" : "confirmation";
    }

    private BigDecimal cartTotal(CartDTO cart) {
        return cart.getItems().stream()
                .map(item -> PriceService.effectivePrice(item.getPriceSnapshot()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
