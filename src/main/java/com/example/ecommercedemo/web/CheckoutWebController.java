package com.example.ecommercedemo.web;

import com.example.ecommercedemo.components.carts.CartCookie;
import com.example.ecommercedemo.dtos.carts.CartDTO;
import com.example.ecommercedemo.dtos.orders.OrderDTO;
import com.example.ecommercedemo.dtos.shipments.ShippingPresetDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.models.carts.CartOwner;
import com.example.ecommercedemo.payments.PaymentGateway;
import com.example.ecommercedemo.payments.PaymentRequest;
import com.example.ecommercedemo.payments.PaymentResult;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.checkouts.CheckoutService;
import com.example.ecommercedemo.services.pricing.PriceService;
import com.example.ecommercedemo.services.shipping.ShippingPolicy;
import com.example.ecommercedemo.services.shipping.ShippingPresetService;
import com.example.ecommercedemo.web.forms.CheckoutForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutWebController {

    private static final int MAX_SAVED_ADDRESSES = 20;

    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final ShippingPresetService shippingPresetService;
    private final ShippingPolicy shippingPolicy;
    private final PaymentGateway paymentGateway;

    @Value("${app.currency:USD}")
    private String currency;

    @GetMapping
    public String form(@CookieValue(name = CartCookie.NAME, required = false) UUID suid,
                       @AuthenticationPrincipal User user,
                       RedirectAttributes redirect,
                       Model model) {
        CartDTO cart = cartService.findCart(CartOwner.of(user, suid)).orElseGet(CartDTO::empty);
        if (cart.getItems().isEmpty()) {
            redirect.addFlashAttribute("flashError", "Your cart is empty — add something before checking out.");
            return "redirect:/cart";
        }
        populateCheckoutModel(model, cart, user);
        if (!model.containsAttribute("checkoutForm")) {
            model.addAttribute("checkoutForm", prefilledForm(user));
        }
        return "checkout";
    }

    @PostMapping
    public String place(@Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                        BindingResult binding,
                        @CookieValue(name = CartCookie.NAME, required = false) UUID suid,
                        @AuthenticationPrincipal User user,
                        @RequestParam(name = "shippingPresetId", required = false) Long shippingPresetId,
                        @RequestParam(name = "savePreset", defaultValue = "false") boolean savePreset,
                        Model model,
                        RedirectAttributes redirect) {

        CartOwner owner = CartOwner.of(user, suid);
        CartDTO cart = cartService.findCart(owner).orElseGet(CartDTO::empty);

        if (cart.getItems().isEmpty()) {
            redirect.addFlashAttribute("flashError", "Your cart is empty.");
            return "redirect:/cart";
        }

        boolean usingSavedAddress = user != null && shippingPresetId != null;
        if (binding.hasErrors() && !usingSavedAddress) {
            populateCheckoutModel(model, cart, user);
            model.addAttribute("fieldErrors", WebErrors.fieldErrors(binding));
            model.addAttribute("selectedPresetId", shippingPresetId);
            model.addAttribute("savePresetChecked", savePreset);
            return "checkout";
        }

        BigDecimal subtotal = cartTotal(cart);
        BigDecimal shippingFee = shippingPolicy.feeFor(subtotal);

        PaymentResult payment = paymentGateway.charge(new PaymentRequest(
                subtotal.add(shippingFee),
                currency,
                form.getMethod(),
                form.getCardNumber(),
                form.getCardHolder(),
                form.getExpiry(),
                form.getCvc()));

        if (!payment.success()) {
            populateCheckoutModel(model, cart, user);
            model.addAttribute("paymentError", payment.message());
            model.addAttribute("selectedPresetId", shippingPresetId);
            model.addAttribute("savePresetChecked", savePreset);
            return "checkout";
        }

        OrderDTO order;
        try {
            order = usingSavedAddress
                    ? checkoutService.checkoutSaved(owner, shippingPresetId)
                    : checkoutService.checkoutNew(owner, form.toShippingPreset(), savePreset && user != null);
        } catch (ResponseStatusException stockProblem) {
            redirect.addFlashAttribute("flashError", stockProblem.getReason());
            return "redirect:/cart";
        }

        redirect.addFlashAttribute("order", order);
        redirect.addFlashAttribute("transactionId", payment.transactionId());
        redirect.addFlashAttribute("shippingFee", shippingFee);
        return "redirect:/checkout/confirmation";
    }

    @GetMapping("/confirmation")
    public String confirmation(@ModelAttribute(value = "order", binding = false) OrderDTO order, Model model) {
        if (order == null || order.getId() == null) {
            return "redirect:/";
        }
        BigDecimal subtotal = order.getItems().stream()
                .map(item -> PriceService.effectivePrice(item.getPriceSnapshot()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = shippingPolicy.feeFor(subtotal);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("grandTotal", subtotal.add(shippingFee));
        return "confirmation";
    }

    private void populateCheckoutModel(Model model, CartDTO cart, User user) {
        BigDecimal subtotal = cartTotal(cart);
        BigDecimal shippingFee = shippingPolicy.feeFor(subtotal);
        model.addAttribute("cart", cart);
        model.addAttribute("total", subtotal);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("grandTotal", subtotal.add(shippingFee));
        model.addAttribute("freeShippingThreshold", shippingPolicy.freeThreshold());
        model.addAttribute("amountUntilFreeShipping", shippingPolicy.amountUntilFreeDelivery(subtotal));
        model.addAttribute("paymentProvider", paymentGateway.providerName());
        model.addAttribute("isGuest", user == null);

        if (user != null) {
            List<ShippingPresetDTO> presets = shippingPresetService
                    .getShippingPresetPaged(PageRequest.of(0, MAX_SAVED_ADDRESSES), user)
                    .getContent();
            model.addAttribute("presets", presets);
            presets.stream()
                    .filter(ShippingPresetDTO::isDefault)
                    .map(ShippingPresetDTO::getId)
                    .findFirst()
                    .ifPresent(id -> model.addAttribute("defaultPresetId", id));
        }
    }

    private CheckoutForm prefilledForm(User user) {
        CheckoutForm form = new CheckoutForm();
        if (user != null) {
            form.setFullName((user.getFirstname() + " " + user.getLastname()).trim());
            form.setEmail(user.getEmail());
        }
        return form;
    }

    private BigDecimal cartTotal(CartDTO cart) {
        return cart.getItems().stream()
                .map(item -> PriceService.effectivePrice(item.getPriceSnapshot()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
