package com.example.ecommercedemo.common;

import com.example.ecommercedemo.dtos.users.CreateUserDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.enums.products.Category;
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
        final var email = "test-" + UUID.randomUUID().toString().substring(0, 8) + "@email.com";

        final var userDto = CreateUserDTO.builder()
                .email(email)
                .firstname("Test")
                .lastname("User")
                .password("password")
                .build();

        userService.createUser(userDto);

        return jwtService.generateToken(email);
    }

    public String authenticateCreatedAdmin() {
        final var email = "test-" + UUID.randomUUID().toString().substring(0, 8) + "@email.com";

        final var userDto = CreateUserDTO.builder()
                .email(email)
                .firstname("Test")
                .lastname("User")
                .password("password")
                .build();
        var user = userService.createUser(userDto);
        userService.makeAdmin(user.getId());

        return jwtService.generateToken(email);
    }

    public Product getSavedProduct() {
        return getSavedProduct("TEST PRODUCT", "TEST DESCRIPTION", Category.DEMO1);
    }

    public Product getSavedProduct(String name, String description, Category category) {
        final var product = Product.builder()
                .name(name)
                .description(description)
                .basePrice(BigDecimal.valueOf(100))
                .discountPercentage(BigDecimal.valueOf(10))
                .salePrice(BigDecimal.valueOf(90))
                .category(category)
                .mediaList(List.of())
                .build();

        return productRepo.save(product);
    }

    public Cart getSavedCart() {
        return Cart.builder().build();
    }
}