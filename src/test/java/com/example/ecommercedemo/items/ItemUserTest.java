package com.example.ecommercedemo.items;

import com.example.ecommercedemo.common.Helpers;
import com.example.ecommercedemo.dtos.items.CreateItemDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.services.carts.CartService;
import com.example.ecommercedemo.services.items.ItemService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:products-test;MODE=MySQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class ItemUserTest  {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ItemService itemService;

    @Autowired
    private Helpers helpers;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addItemToCart_User_success() throws Exception {
        var user = helpers.createMockUser();
        var token = helpers.authenticateCreatedUser(user);

        cartRepo.save(
                Cart.builder()
                        .user(user)
                        .build()
        );

        Product product = helpers.getSavedProduct();

        CreateItemDTO dto = CreateItemDTO.builder()
                .productId(product.getId())
                .quantity(2)
                .build();

        mockMvc.perform(
                        post("/api/cart/items")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price.totalPrice").value(180.0));
    }

    @Test
    void decrementItemFromCart_User_success() throws Exception {
        var user = helpers.createMockUser();
        var token = helpers.authenticateCreatedUser(user);

        cartRepo.save(
                Cart.builder()
                        .user(user)
                        .build()
        );

        Product product = helpers.getSavedProduct();

        CreateItemDTO dto = CreateItemDTO.builder()
                .productId(product.getId())
                .quantity(2)
                .build();

        mockMvc.perform(
                        post("/api/cart/items")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/api/cart/items")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("productId", product.getId().toString())
                                .param("quantity", "1")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price.totalPrice").value(90.0));
    }

    @Test
    void decrementItemFromCartDelete_User_success() throws Exception {
        var user = helpers.createMockUser();
        var token = helpers.authenticateCreatedUser(user);

        cartRepo.save(
                Cart.builder()
                        .user(user)
                        .build()
        );

        Product product = helpers.getSavedProduct();

        CreateItemDTO dto = CreateItemDTO.builder()
                .productId(product.getId())
                .quantity(2)
                .build();

        mockMvc.perform(
                        post("/api/cart/items")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/api/cart/items")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("productId", product.getId().toString())
                                .param("quantity", "4")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price.totalPrice").value(0.00));
    }

    @Test
    void removeItemFromCart_User_success() throws Exception {
        var user = helpers.createMockUser();
        var token = helpers.authenticateCreatedUser(user);

        cartRepo.save(
                Cart.builder()
                        .user(user)
                        .build()
        );

        Product product = helpers.getSavedProduct();

        CreateItemDTO dto = CreateItemDTO.builder()
                .productId(product.getId())
                .quantity(3)
                .build();

        mockMvc.perform(
                        post("/api/cart/items")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/api/cart/items/all")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("productId", product.getId().toString())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price.totalPrice").value(0.00));
    }
}
