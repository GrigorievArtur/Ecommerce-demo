package com.example.ecommercedemo.web.forms;

import com.example.ecommercedemo.dtos.products.CreateProductDTO;
import com.example.ecommercedemo.dtos.products.ProductDTO;
import com.example.ecommercedemo.dtos.products.ProductMediaDTO;
import com.example.ecommercedemo.dtos.products.UpdateProductDTO;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.enums.products.MediaTypes;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Backing bean for the admin create/edit product screen.
 */
@Data
public class ProductForm {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 150, message = "Name must be 3–150 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be 10–5000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    private BigDecimal basePrice;

    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @NotNull(message = "Category is required")
    private Category category;

    /** Blank or negative is treated as untracked stock. */
    private Integer stock;

    /** Single image URL for the demo; the domain model already supports a media list. */
    private String imageUrl;

    public CreateProductDTO toCreateDTO() {
        return CreateProductDTO.builder()
                .name(name)
                .description(description)
                .basePrice(basePrice)
                .discountPercentage(discountPercentage)
                .category(category)
                .stock(normalizeStock())
                .mediaList(media())
                .build();
    }

    public UpdateProductDTO toUpdateDTO() {
        UpdateProductDTO dto = new UpdateProductDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setBasePrice(basePrice);
        dto.setDiscountPercentage(discountPercentage);
        dto.setCategory(category);
        dto.setStock(normalizeStock());
        dto.setMediaList(media());
        return dto;
    }

    public static ProductForm from(ProductDTO product) {
        ProductForm form = new ProductForm();
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setCategory(product.getCategory());
        form.setStock(product.getStock());
        if (product.getPrice() != null) {
            form.setBasePrice(product.getPrice().getGrossPrice());
            form.setDiscountPercentage(product.getPrice().getPercentageDiscount());
        }
        if (product.getMediaList() != null && !product.getMediaList().isEmpty()) {
            form.setImageUrl(product.getMediaList().get(0).getUrl());
        }
        return form;
    }

    public String getCategoryName() {
        return category == null ? null : category.name();
    }

    private Integer normalizeStock() {
        return (stock == null || stock < 0) ? null : stock;
    }

    private List<ProductMediaDTO> media() {
        if (imageUrl == null || imageUrl.isBlank()) {
            return List.of();
        }
        return List.of(ProductMediaDTO.builder()
                .mediaType(MediaTypes.IMAGE)
                .url(imageUrl.trim())
                .build());
    }
}
