package com.example.ecommercedemo.web;

import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.filters.products.ProductFilter;
import com.example.ecommercedemo.services.products.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StorefrontController {

    private static final int MAX_PAGE_SIZE = 48;
    private static final int RELATED_PRODUCTS = 4;

    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        Page<ProductDTO> featured = productService.getAllProducts(
                new ProductFilter(),
                PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id")));
        model.addAttribute("products", featured.getContent());
        return "home";
    }

    @GetMapping("/products")
    public String catalog(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "category", required = false) Category category,
            @RequestParam(name = "sort", defaultValue = "newest") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            Model model) {

        ProductFilter filter = ProductFilter.builder().query(query).category(category).build();
        Page<ProductDTO> products = productService.getAllProducts(
                filter,
                PageRequest.of(Math.max(page, 0), clampSize(size), sortOf(sort)));

        model.addAttribute("products", products);
        model.addAttribute("q", query);
        model.addAttribute("selectedCategory", category == null ? null : CategoryView.from(category));
        model.addAttribute("sort", sort);
        return "products";
    }

    @GetMapping("/products/{id}")
    public String product(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("related", relatedProducts(product));
        return "product";
    }

    @GetMapping("/returns")
    public String returns() {
        return "returns";
    }

    /** Other live products from the same category, for the "You may also like" rail. */
    private List<ProductDTO> relatedProducts(ProductDTO product) {
        if (product.getCategory() == null) {
            return List.of();
        }
        Page<ProductDTO> sameCategory = productService.getAllProducts(
                ProductFilter.builder().category(product.getCategory()).build(),
                PageRequest.of(0, RELATED_PRODUCTS + 1, Sort.by(Sort.Direction.DESC, "id")));
        return sameCategory.getContent().stream()
                .filter(candidate -> !candidate.getId().equals(product.getId()))
                .limit(RELATED_PRODUCTS)
                .toList();
    }

    private int clampSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private Sort sortOf(String sort) {
        return switch (sort) {
            case "price-asc" -> Sort.by(Sort.Direction.ASC, "price.grossPrice");
            case "price-desc" -> Sort.by(Sort.Direction.DESC, "price.grossPrice");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }
}
