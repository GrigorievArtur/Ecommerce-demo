package com.example.ecommercedemo.web;

import com.example.ecommercedemo.dtos.users.CreateUserDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Admin pages must render for an ADMIN user — including the "new product" form, which
 * previously failed when the category was empty.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:web-admin-test;MODE=MySQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.seed.enabled=false"
})
@AutoConfigureMockMvc
class AdminWebTest {

    private static final Pattern CSRF = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepo productRepo;

    private MockHttpSession session;
    private Product product;

    @BeforeEach
    void loginAsAdmin() throws Exception {
        product = productRepo.save(Product.builder()
                .name("Admin Test Set")
                .description("A product used by the admin rendering test.")
                .category(Category.DEMO2)
                .stock(3)
                .price(Price.builder()
                        .grossPrice(new BigDecimal("30.00"))
                        .percentageDiscount(BigDecimal.ZERO)
                        .quantity(BigDecimal.ONE)
                        .build())
                .build());

        User admin = userService.createUser(CreateUserDTO.builder()
                .firstname("Ada")
                .lastname("Admin")
                .email("admin-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                .password("password123")
                .build());
        userService.makeAdmin(admin.getId());

        session = new MockHttpSession();
        String csrf = extractCsrf(mockMvc.perform(get("/login").session(session))
                .andReturn().getResponse().getContentAsString());
        mockMvc.perform(post("/login").session(session)
                .param("username", admin.getEmail())
                .param("password", "password123")
                .param("_csrf", csrf));
    }

    @Test
    void dashboard_renders() throws Exception {
        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dashboard")))
                .andExpect(content().string(containsString("class=\"site-header\"")));
    }

    @Test
    void productList_renders() throws Exception {
        mockMvc.perform(get("/admin/products").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin Test Set")));
    }

    @Test
    void newProductForm_renders_withEmptyCategory() throws Exception {
        mockMvc.perform(get("/admin/products/new").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("New product")))
                .andExpect(content().string(containsString("name=\"category\"")));
    }

    @Test
    void editProductForm_renders_withSelectedCategory() throws Exception {
        mockMvc.perform(get("/admin/products/" + product.getId() + "/edit").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin Test Set")))
                .andExpect(content().string(containsString("value=\"DEMO2\" selected")));
    }

    private String extractCsrf(String html) {
        Matcher matcher = CSRF.matcher(html);
        if (!matcher.find()) {
            throw new IllegalStateException("No CSRF token found in page");
        }
        return matcher.group(1);
    }
}
