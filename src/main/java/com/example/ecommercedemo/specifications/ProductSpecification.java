package com.example.ecommercedemo.specifications;

import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.filters.products.ProductFilter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor
public class ProductSpecification {
    public static Specification<Product> withFilter(ProductFilter filter) {
        return Specification
                .where(hasQuery(filter.getQuery()))
                .and(hasCategory(filter.getCategory()));
    }

    private static Specification<Product> hasCategory(Category category) {
        return (root, cp, cb) -> {
            if (category == null)
                return null;
            return cb.equal(root.get("category"), category);
        };
    }

    private static Specification<Product> hasQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank()) {
                return null;
            }

            String likeQuery = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), likeQuery),
                    cb.like(cb.lower(root.get("description")), likeQuery)
            );
        };
    }

}
