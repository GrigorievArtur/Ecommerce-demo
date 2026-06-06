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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
public class ItemGuestTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartService cartService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private Helpers helpers;

    @Autowired
    private ObjectMapper objectMapper;



    @Test
    void addItemToCart_Guest_success() throws Exception {
        var cart = cartRepo.save(new Cart());
        var suid = cart.getSuid();
        Product product = helpers.getSavedProduct();

        CreateItemDTO createItemDTO = CreateItemDTO.builder().productId(product.getId()).quantity(2).build();

        mockMvc.perform(
                        post("/api/cart/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createItemDTO))
                                .cookie(new Cookie("suid", suid.toString()))
                )
                .andDo(print())
                .andExpect(jsonPath("$.finalPrice").value(BigDecimal.valueOf(180.0000)))
                .andExpect(status().isOk());
    }

    @Test
    void decrementItemFromCart_Guest_success() throws Exception {
        var cart = cartRepo.save(new Cart());
        var suid = cart.getSuid();
        Product product = helpers.getSavedProduct();

        CreateItemDTO createItemDTO = CreateItemDTO.builder().productId(product.getId()).quantity(2).build();
        itemService.addCartItem(suid, createItemDTO);


        mockMvc.perform(
                        delete("/api/cart/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("productId", product.getId().toString())
                                .param("quantity", String.valueOf(1))
                                .cookie(new Cookie("suid", suid.toString()))
                )
                .andDo(print())
                .andExpect(jsonPath("$.finalPrice").value(BigDecimal.valueOf(90.0000)))
                .andExpect(status().isOk());
    }

    @Test
    void decrementItemFromCartDelete_Guest_success() throws Exception {
        var cart = cartRepo.save(new Cart());
        var suid = cart.getSuid();
        Product product = helpers.getSavedProduct();

        CreateItemDTO createItemDTO = CreateItemDTO.builder().productId(product.getId()).quantity(2).build();
        itemService.addCartItem(suid, createItemDTO);


        mockMvc.perform(
                        delete("/api/cart/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("productId", product.getId().toString())
                                .param("quantity", String.valueOf(4))
                                .cookie(new Cookie("suid", suid.toString()))
                )
                .andDo(print())
                .andExpect(jsonPath("$.finalPrice").value(BigDecimal.valueOf(0.0000)))
                .andExpect(status().isOk());
    }

    @Test
    void removeItemFromCart_Guest_success() throws Exception {
        var cart = cartRepo.save(new Cart());
        var suid = cart.getSuid();
        Product product = helpers.getSavedProduct();

        CreateItemDTO createItemDTO = CreateItemDTO.builder().productId(product.getId()).quantity(3).build();
        itemService.addCartItem(suid, createItemDTO);


        mockMvc.perform(
                        delete("/api/cart/items/all")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("productId", product.getId().toString())
                                .cookie(new Cookie("suid", suid.toString()))
                )
                .andDo(print())
                .andExpect(jsonPath("$.finalPrice").value(BigDecimal.valueOf(0.0000)))
                .andExpect(status().isOk());
    }


}
