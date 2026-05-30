package com.example.ecommercedemo.products;

import com.example.ecommercedemo.common.Helpers;
import com.example.ecommercedemo.dtos.users.CreateUserDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.jwt.JwtService;
import com.example.ecommercedemo.services.users.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class ProductsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private Helpers helpers;

    @BeforeEach
    void setUp() {
        productRepo.deleteAll();
    }

    //********************************************************************
    // GET
    //********************************************************************

    @Test
    void getProductsPage_filtersProducts_success() throws Exception {
//        final var token = authenticateCreatedUser();
        final var matchingProduct = helpers.getSavedProduct("Test phone", "Android phone", Category.DEMO1);
        helpers.getSavedProduct("Test laptop", "Work laptop", Category.DEMO2);

        final var currentProductSize = productRepo.count();

        mockMvc.perform(
                        get("/api/products")
//                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("query", "phone")
                                .param("category", "DEMO1")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value(matchingProduct.getName()))
                .andExpect(jsonPath("$.content[0].description").value(matchingProduct.getDescription()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andReturn();

        Assertions.assertEquals(currentProductSize, productRepo.count());
    }

    @Test
    void getProductsPage_noFiltersProducts_success() throws Exception {
//        final var token = authenticateCreatedUser();
        final var matchingProduct = helpers.getSavedProduct("Test phone", "Android phone", Category.DEMO1);
        helpers.getSavedProduct("Test laptop", "Work laptop", Category.DEMO2);

        final var currentProductSize = productRepo.count();

        mockMvc.perform(
                        get("/api/products")
//                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("page", "0")
                                .param("size", "10")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value(matchingProduct.getName()))
                .andExpect(jsonPath("$.content[0].description").value(matchingProduct.getDescription()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andReturn();

        Assertions.assertEquals(currentProductSize, productRepo.count());
    }

}
