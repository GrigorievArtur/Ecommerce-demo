package com.example.ecommercedemo.web;

import com.example.ecommercedemo.dtos.users.CreateUserDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.repositories.orders.OrderRepo;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full browser-style flow: form login → add to cart → pay → confirmation.
 * Exercises the session security chain, CSRF and the mock payment gateway.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:web-checkout-test;MODE=MySQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.seed.enabled=false"
})
@AutoConfigureMockMvc
class CheckoutWebFlowTest {

    private static final Pattern CSRF = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private UserService userService;

    private String email;
    private Product product;

    @BeforeEach
    void setUp() {
        email = "buyer-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        userService.createUser(CreateUserDTO.builder()
                .firstname("Jane")
                .lastname("Doe")
                .email(email)
                .password("password123")
                .build());

        orderRepo.deleteAll();
        productRepo.deleteAll();
        product = productRepo.save(Product.builder()
                .name("Checkout Test Set")
                .description("A product used by the checkout flow test.")
                .category(Category.DEMO3)
                .stock(5)
                .price(Price.builder()
                        .grossPrice(new BigDecimal("20.00"))
                        .percentageDiscount(BigDecimal.ZERO)
                        .quantity(BigDecimal.ONE)
                        .build())
                .build());
    }

    @Test
    void login_addToCart_pay_producesOrderAndDecrementsStock() throws Exception {
        MockHttpSession session = new MockHttpSession();

        String loginPage = mockMvc.perform(get("/login").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String csrf = extractCsrf(loginPage);

        mockMvc.perform(post("/login").session(session)
                        .param("username", email)
                        .param("password", "password123")
                        .param("_csrf", csrf))
                .andExpect(status().is3xxRedirection());

        // Re-read a fresh token after the session-fixation change.
        String homePage = mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        csrf = extractCsrf(homePage);

        mockMvc.perform(post("/cart/items").session(session)
                        .param("productId", product.getId().toString())
                        .param("quantity", "2")
                        .param("_csrf", csrf))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        mockMvc.perform(post("/checkout").session(session)
                        .param("_csrf", csrf)
                        .param("fullName", "Jane Doe")
                        .param("email", email)
                        .param("phoneNumber", "123456789")
                        .param("country", "US")
                        .param("state", "CA")
                        .param("city", "Los Angeles")
                        .param("street", "123 Main St")
                        .param("method", "CARD")
                        .param("cardNumber", "4242424242424242")
                        .param("cardHolder", "Jane Doe")
                        .param("expiry", "12/28")
                        .param("cvc", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout/confirmation"));

        mockMvc.perform(get("/checkout/confirmation").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("confirmed")));

        assertEquals(1, orderRepo.count());
        assertEquals(3, productRepo.findById(product.getId()).orElseThrow().getStock());
    }

    @Test
    void declinedCardDoesNotCreateOrder() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String loginPage = mockMvc.perform(get("/login").session(session)).andReturn().getResponse().getContentAsString();
        String csrf = extractCsrf(loginPage);
        mockMvc.perform(post("/login").session(session)
                .param("username", email).param("password", "password123").param("_csrf", csrf));

        String homePage = mockMvc.perform(get("/").session(session)).andReturn().getResponse().getContentAsString();
        csrf = extractCsrf(homePage);

        mockMvc.perform(post("/cart/items").session(session)
                .param("productId", product.getId().toString())
                .param("quantity", "1")
                .param("_csrf", csrf));

        mockMvc.perform(post("/checkout").session(session)
                        .param("_csrf", csrf)
                        .param("fullName", "Jane Doe").param("email", email).param("phoneNumber", "123")
                        .param("country", "US").param("state", "CA").param("city", "LA").param("street", "1 Main")
                        .param("method", "CARD")
                        .param("cardNumber", "4000000000000000") // ends in 0000 → declined
                        .param("cardHolder", "Jane Doe").param("expiry", "12/28").param("cvc", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("declined")));

        assertEquals(0, orderRepo.count());
    }

    private String extractCsrf(String html) {
        Matcher matcher = CSRF.matcher(html);
        if (!matcher.find()) {
            throw new IllegalStateException("No CSRF token found in page");
        }
        return matcher.group(1);
    }
}
