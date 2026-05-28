package com.example.ecommercedemo.models.products;

import com.example.ecommercedemo.enums.products.MediaTypes;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ProductMedia {
    @Enumerated(EnumType.STRING)
    private MediaTypes mediaType;
    private String url;
}
