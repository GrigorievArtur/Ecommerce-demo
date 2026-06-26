package com.example.ecommercedemo.dtos.carts;

import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCartDTO {
    private List<CreateItemDTO> items = new ArrayList<>();
}
