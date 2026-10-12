package com.example.ecommercedemo.enums.products;

/**
 * Product categories. Stored by name; the label is presentation-only.
 *
 * <p>TODO: replace the DEMO placeholders with the real catalogue taxonomy.</p>
 */
public enum Category {
    DEMO1("Bricks & Sets"),
    DEMO2("Minifigures"),
    DEMO3("Accessories");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
