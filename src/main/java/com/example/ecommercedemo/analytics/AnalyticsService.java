package com.example.ecommercedemo.analytics;

import com.example.ecommercedemo.entities.orders.OrderItem;
import com.example.ecommercedemo.repositories.orders.OrderRepo;
import com.example.ecommercedemo.services.pricing.PriceService;
import com.example.ecommercedemo.services.products.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Lightweight sales analytics rendered on the admin dashboard.
 *
 * <p>Revenue is derived from the frozen {@code PriceSnapshot} on each order line, so
 * historic figures are stable even when a product's price changes later. For behavioural
 * analytics (funnels, sessions) plug in GA4 / Plausible rather than rolling your own —
 * see {@code README-HANDOFF.md}.</p>
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final OrderRepo orderRepo;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public SalesSummary summary() {
        var orders = orderRepo.findAll();

        BigDecimal revenue = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItem::getPriceSnapshot)
                .map(PriceService::effectivePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal averageOrderValue = orders.isEmpty()
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        return new SalesSummary(
                productService.count(),
                orders.size(),
                revenue,
                averageOrderValue,
                productService.getLowStockProducts(LOW_STOCK_THRESHOLD)
        );
    }
}
