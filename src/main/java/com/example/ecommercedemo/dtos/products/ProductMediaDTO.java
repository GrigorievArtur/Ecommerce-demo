package com.example.ecommercedemo.dtos.products;

import com.example.ecommercedemo.enums.products.MediaTypes;
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
public class ProductMediaDTO {

    @Enumerated(EnumType.STRING)
    private MediaTypes mediaType;
    private String url;
}
