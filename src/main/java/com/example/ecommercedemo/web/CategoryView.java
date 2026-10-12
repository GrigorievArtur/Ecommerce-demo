package com.example.ecommercedemo.web;

import com.example.ecommercedemo.enums.products.Category;
import lombok.Getter;

/**
 * Template-friendly view of a {@link Category} (plain getters, no enum introspection).
 */
@Getter
public class CategoryView {

    private final String name;
    private final String label;

    public CategoryView(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public static CategoryView from(Category category) {
        return new CategoryView(category.name(), category.getLabel());
    }
}
