package com.example.ecommercedemo.web;

import com.example.ecommercedemo.components.auth.SecurityHelper;
import com.example.ecommercedemo.components.carts.CartCookie;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.carts.CartOwner;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.shipping.ShippingPolicy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Adds layout-level data (nav user, cart badge, categories, CSRF token) to every
 * server-rendered page. Scoped to the web package so REST responses are untouched.
 */
@ControllerAdvice(basePackages = "com.example.ecommercedemo.web")
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final SecurityHelper securityHelper;
    private final CartService cartService;
    private final ShippingPolicy shippingPolicy;

    @Value("${app.name:${spring.application.name:Store}}")
    private String appName;

    @Value("${app.currency:USD}")
    private String currency;

    @Value("${app.currency-symbol:$}")
    private String currencySymbol;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @ModelAttribute("currentUser")
    public CurrentUser currentUser() {
        return securityHelper.getCurrentUser().map(CurrentUser::from).orElse(null);
    }

    /** Read-only cart badge; rendering a page never creates a cart. */
    @ModelAttribute("cartCount")
    public long cartCount(@CookieValue(name = CartCookie.NAME, required = false) UUID suid) {
        CartOwner owner = CartOwner.of(securityHelper.getCurrentUser().orElse(null), suid);
        return cartService.findCartEntityOrEmpty(owner)
                .map(cart -> cart.getItems().stream()
                        .mapToLong(item -> item.getPriceSnapshot().getQuantity().longValue())
                        .sum())
                .orElse(0L);
    }

    @ModelAttribute("categories")
    public List<CategoryView> categories() {
        return Arrays.stream(Category.values()).map(CategoryView::from).toList();
    }

    @ModelAttribute("csrf")
    public CsrfToken csrf(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }

    /** Request path without the query string — used to highlight the active nav item. */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null ? "/" : uri;
    }

    @ModelAttribute("appName")
    public String appName() {
        return appName;
    }

    @ModelAttribute("currency")
    public String currency() {
        return currency;
    }

    @ModelAttribute("currencySymbol")
    public String currencySymbol() {
        return currencySymbol;
    }

    @ModelAttribute("freeShippingThreshold")
    public java.math.BigDecimal freeShippingThreshold() {
        return shippingPolicy.freeThreshold();
    }

    @ModelAttribute("shippingFlatRate")
    public java.math.BigDecimal shippingFlatRate() {
        return shippingPolicy.flatRate();
    }

    @ModelAttribute("canonicalUrl")
    public String canonicalUrl(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || path.isBlank() ? baseUrl + "/" : baseUrl + path;
    }

    @ModelAttribute("year")
    public int year() {
        return Year.now().getValue();
    }
}
