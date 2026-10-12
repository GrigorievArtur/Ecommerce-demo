package com.example.ecommercedemo.analytics;

import com.example.ecommercedemo.dtos.products.ProductDTO;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/** Template-friendly sales figures for the admin dashboard. */
@Getter
public class SalesSummary {

    private final long totalProducts;
    private final long totalOrders;
    private final BigDecimal revenue;
    private final BigDecimal averageOrderValue;
    private final List<ProductDTO> lowStock;

    public SalesSummary(long totalProducts,
                        long totalOrders,
                        BigDecimal revenue,
                        BigDecimal averageOrderValue,
                        List<ProductDTO> lowStock) {
        this.totalProducts = totalProducts;
        this.totalOrders = totalOrders;
        this.revenue = revenue;
        this.averageOrderValue = averageOrderValue;
        this.lowStock = lowStock;
    }
}
