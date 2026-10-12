package com.example.ecommercedemo.web;

import com.example.ecommercedemo.components.auth.SecurityHelper;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.services.carts.CartService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Adds layout-level data (nav user, cart badge, categories, CSRF token) to every
 * server-rendered page. Scoped to the web package so REST responses are untouched.
 */
@ControllerAdvice(basePackages = "com.example.ecommercedemo.web")
@RequiredArgsConstructor
public class GlobalModelAttributes {

    public static final String CART_COOKIE = "suid";

    private final SecurityHelper securityHelper;
    private final CartService cartService;

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

    @ModelAttribute("cartCount")
    public long cartCount(@CookieValue(name = CART_COOKIE, required = false) UUID suid) {
        try {
            return cartService.getCartDTO(suid).getItems().stream()
                    .mapToLong(item -> item.getPriceSnapshot().getQuantity().longValue())
                    .sum();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    @ModelAttribute("categories")
    public List<CategoryView> categories() {
        return java.util.Arrays.stream(Category.values()).map(CategoryView::from).toList();
    }

    @ModelAttribute("csrf")
    public CsrfToken csrf(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
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

    @ModelAttribute("canonicalUrl")
    public String canonicalUrl(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || path.isBlank() ? baseUrl + "/" : baseUrl + path;
    }

    @ModelAttribute("year")
    public int year() {
        return Year.now().getValue();
    }

    /** Convenience for controllers that need to persist the cart cookie. */
    public static void setCartCookie(jakarta.servlet.http.HttpServletResponse response, UUID suid) {
        if (suid == null) {
            return;
        }
        Cookie cookie = new Cookie(CART_COOKIE, suid.toString());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(cookie);
    }
}
