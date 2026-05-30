package com.example.ecommercedemo.products;

import com.example.ecommercedemo.common.Helpers;
import com.example.ecommercedemo.dtos.products.CreateProductDTO;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.mappers.products.ProductMapper;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.products.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
public class ProductsAdminTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private Helpers helpers;



    @Test
    void addProduct_Admin_success() throws Exception {
        var objectMapper = new ObjectMapper();
        var admin_token = helpers.authenticateCreatedAdmin();

        final var product_dto = CreateProductDTO.builder()
                .name("Test Product")
                .description("Test Description")
                .basePrice(new BigDecimal("10"))
                .salePrice(new BigDecimal("10"))
                .discountPercentage(new BigDecimal("0"))
                .category(Category.DEMO1)
                .build();

        mockMvc.perform(
                        post("/api/admin/products")
                                .header("Authorization", "Bearer " + admin_token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(product_dto))
                )
                .andDo(print())
                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void addProduct_Admin_failed() throws Exception {
        var objectMapper = new ObjectMapper();
        var admin_token = helpers.authenticateCreatedUser();

        final var product_dto = CreateProductDTO.builder()
                .name("Test Product")
                .description("Test Description")
                .basePrice(new BigDecimal("10"))
                .salePrice(new BigDecimal("10"))
                .discountPercentage(new BigDecimal("0"))
                .category(Category.DEMO1)
                .build();

        mockMvc.perform(
                        post("/api/admin/products")
                                .header("Authorization", "Bearer " + admin_token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(product_dto))
                )
                .andDo(print())
                .andExpect(status().isForbidden());

    }


    //********************************************************************
    // UPDATE
    //********************************************************************
    @Test
    void updateProduct_Admin_success() throws Exception {
        var objectMapper = new ObjectMapper();
        var admin_token = helpers.authenticateCreatedAdmin();

        var product = helpers.getSavedProduct();
        var product_dto = productMapper.toDTO(product);

        product_dto.setName("Updated Test Product");

        mockMvc.perform(
                patch("/api/admin/products")
                        .header("Authorization", "Bearer " + admin_token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product_dto))
                        .param("id", product.getId().toString())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Test Product"));

    }

    @Test
    void updateProduct_Admin_failed() throws Exception {
        var objectMapper = new ObjectMapper();
        var admin_token = helpers.authenticateCreatedUser();


        var product = helpers.getSavedProduct();
        var product_dto = productMapper.toDTO(product);

        product_dto.setName("Updated Test Product");

        mockMvc.perform(
                        patch("/api/admin/products")
                                .header("Authorization", "Bearer " + admin_token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(product_dto))
                                .param("id", product.getId().toString())
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    //********************************************************************
    // DELETE
    //********************************************************************
    @Test
    void deleteProduct_Admin_success() throws Exception {
        var admin_token = helpers.authenticateCreatedAdmin();
        var product = helpers.getSavedProduct();

        mockMvc.perform(
                        delete("/api/admin/products")
                                .header("Authorization", "Bearer " + admin_token)
                                .param("id", product.getId().toString())
                )
                .andDo(print())
                .andExpect(status().isOk());

    }
    @Test
    void deleteProduct_Admin_failed() throws Exception {
        var admin_token = helpers.authenticateCreatedUser();
        var product = helpers.getSavedProduct();

        mockMvc.perform(
                        delete("/api/admin/products")
                                .header("Authorization", "Bearer " + admin_token)
                                .param("id", product.getId().toString())
                )
                .andDo(print())
                .andExpect(status().isForbidden());

    }
    @Test
    void deleteProduct_Admin_notFound() throws Exception {
        var admin_token = helpers.authenticateCreatedAdmin();
        var missingId = Long.MAX_VALUE;

        mockMvc.perform(
                        delete("/api/admin/products")
                                .header("Authorization", "Bearer " + admin_token)
                                .param("id", String.valueOf(missingId))
                )
                .andDo(print())
                .andExpect(status().isNotFound());

    }

}
