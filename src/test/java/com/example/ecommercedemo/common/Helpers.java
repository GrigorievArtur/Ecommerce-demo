package com.example.ecommercedemo.common;

import com.example.ecommercedemo.dtos.users.CreateUserDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.pricing.BasePrice;
import com.example.ecommercedemo.models.pricing.UnitPrice;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.jwt.JwtService;
import com.example.ecommercedemo.services.users.UserService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class Helpers {

    private final ProductRepo productRepo;
    private final UserService userService;
    private final JwtService jwtService;

    public Helpers(
            ProductRepo productRepo,
            UserService userService,
            JwtService jwtService
    ) {
        this.productRepo = productRepo;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public String authenticateCreatedUser() {
        return jwtService.generateToken(createMockUser().getEmail());
    }

    public String authenticateCreatedUser(User user) {
        return jwtService.generateToken(user.getEmail());
    }


    public User createMockUser(){
        final var email = "test-" + UUID.randomUUID().toString().substring(0, 8) + "@email.com";
        final var userDto = CreateUserDTO.builder()
                .email(email)
                .firstname("Test")
                .lastname("User")
                .password("password")
                .build();

        return userService.createUser(userDto);
    }

    public String authenticateCreatedAdmin() {
        var user = createMockUser();
        userService.makeAdmin(user.getId());

        return jwtService.generateToken(user.getEmail());
    }

    public Product getSavedProduct() {
        return getSavedProduct("TEST PRODUCT", "TEST DESCRIPTION", Category.DEMO1);
    }

    public Product getSavedProduct(String name, String description, Category category) {
        final var product = Product.builder()
                .name(name)
                .description(description)
                .unitPrice(UnitPrice.builder()
                        .basePrice(
                                BasePrice.builder()
                                        .price(BigDecimal.valueOf(100))
                                        .build())
                        .discountPercentage(BigDecimal.valueOf(10))
                        .build())
                .category(category)
                .mediaList(List.of())
                .build();

        return productRepo.save(product);
    }

}