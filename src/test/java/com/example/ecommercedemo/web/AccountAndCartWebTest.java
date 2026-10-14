package com.example.ecommercedemo.web;

import com.example.ecommercedemo.dtos.users.CreateUserDTO;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import com.example.ecommercedemo.services.users.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers the account address book, guest-cart merging at login and the cart/checkout
 * guards introduced alongside the storefront polish.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:account-cart-test;MODE=MySQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.seed.enabled=false",
        "app.shipping.free-threshold=75",
        "app.shipping.flat-rate=5.99"
})
@AutoConfigureMockMvc
class AccountAndCartWebTest {

    private static final Pattern CSRF = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private UserService userService;

    private String email;
    private Product product;

    @BeforeEach
    void setUp() {
        email = "shopper-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        userService.createUser(CreateUserDTO.builder()
                .firstname("Sam")
                .lastname("Shopper")
                .email(email)
                .password("password123")
                .build());
        product = productRepo.save(Product.builder()
                .name("Account Test Set")
                .description("A product used by the account and cart web tests.")
                .category(Category.DEMO3)
                .stock(10)
                .price(Price.builder()
                        .grossPrice(new BigDecimal("20.00"))
                        .percentageDiscount(BigDecimal.ZERO)
                        .quantity(BigDecimal.ONE)
                        .build())
                .build());
    }

    @Test
    void address_canBeCreatedEditedDefaultedAndDeleted() throws Exception {
        MockHttpSession session = login();

        String accountCsrf = csrfFromPage(session, "/account");
        mockMvc.perform(post("/account/addresses").session(session)
                        .param("_csrf", accountCsrf)
                        .param("presetName", "Office")
                        .param("fullName", "Sam Shopper")
                        .param("email", email)
                        .param("phoneNumber", "5551234")
                        .param("country", "US")
                        .param("state", "CA")
                        .param("city", "San Jose")
                        .param("street", "12 Test St"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"));

        String account = mockMvc.perform(get("/account").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("12 Test St")))
                .andReturn().getResponse().getContentAsString();
        Long addressId = extractAddressId(account);

        mockMvc.perform(post("/account/addresses/" + addressId + "/default").session(session)
                        .param("_csrf", accountCsrf))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/account").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Default")));

        mockMvc.perform(post("/account/addresses/" + addressId).session(session)
                        .param("_csrf", accountCsrf)
                        .param("presetName", "Office")
                        .param("fullName", "Sam Shopper")
                        .param("email", email)
                        .param("phoneNumber", "5551234")
                        .param("country", "US")
                        .param("state", "CA")
                        .param("city", "San Jose")
                        .param("street", "99 Updated Ave")
                        .param("isDefault", "true"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/account").session(session))
                .andExpect(content().string(containsString("99 Updated Ave")));

        mockMvc.perform(post("/account/addresses/" + addressId + "/delete").session(session)
                        .param("_csrf", accountCsrf))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/account").session(session))
                .andExpect(content().string(not(containsString("99 Updated Ave"))));
    }

    @Test
    void addressValidation_reportsMissingFields() throws Exception {
        MockHttpSession session = login();
        String csrf = csrfFromPage(session, "/account/addresses/new");
        mockMvc.perform(post("/account/addresses").session(session)
                        .param("_csrf", csrf)
                        .param("presetName", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("is required")));
    }

    @Test
    void emptyCartCheckout_redirectsToCart() throws Exception {
        mockMvc.perform(get("/checkout").session(new MockHttpSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void cartBelowThreshold_showsFlatRateDelivery() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrf = csrfFromPage(session, "/");
        Cookie suid = mockMvc.perform(post("/cart/items").session(session)
                        .param("productId", product.getId().toString())
                        .param("quantity", "1")
                        .param("_csrf", csrf))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getCookie("suid");
        assertThat(suid).isNotNull();
        mockMvc.perform(get("/cart").session(session).cookie(suid))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("5.99")));
    }

    @Test
    void guestCartIsMergedIntoUserCartOnLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrf = csrfFromPage(session, "/");
        MvcResult add = mockMvc.perform(post("/cart/items").session(session)
                        .param("productId", product.getId().toString())
                        .param("quantity", "2")
                        .param("_csrf", csrf))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        Cookie suid = add.getResponse().getCookie("suid");
        assertThat(suid).isNotNull();

        String loginCsrf = csrfFromPage(session, "/login");
        mockMvc.perform(post("/login").session(session).cookie(suid)
                        .param("username", email)
                        .param("password", "password123")
                        .param("_csrf", loginCsrf))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/cart").session(session).cookie(suid))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Account Test Set")))
                .andExpect(content().string(containsString(">2<")));
    }

    @Test
    void onlyOneAddressCanBeDefault() throws Exception {
        MockHttpSession session = login();
        String csrf = csrfFromPage(session, "/account");
        createAddress(session, csrf, "Home", "1 First St", true);
        createAddress(session, csrf, "Work", "2 Second St", true);

        String account = mockMvc.perform(get("/account").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long defaultChips = account.split(">Default<", -1).length - 1;
        assertThat(defaultChips)
                .as("the newest default replaces the previous one")
                .isEqualTo(1);
        assertThat(account).contains("2 Second St");
    }

    // ---- helpers ----

    private void createAddress(MockHttpSession session, String csrf, String label, String street, boolean isDefault)
            throws Exception {
        var request = post("/account/addresses").session(session)
                .param("_csrf", csrf)
                .param("presetName", label)
                .param("fullName", "Sam Shopper")
                .param("email", email)
                .param("phoneNumber", "5551234")
                .param("country", "US")
                .param("state", "CA")
                .param("city", "San Jose")
                .param("street", street);
        if (isDefault) {
            request = request.param("isDefault", "true");
        }
        mockMvc.perform(request).andExpect(status().is3xxRedirection());
    }

    private MockHttpSession login() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String csrf = csrfFromPage(session, "/login");
        mockMvc.perform(post("/login").session(session)
                        .param("username", email)
                        .param("password", "password123")
                        .param("_csrf", csrf))
                .andExpect(status().is3xxRedirection());
        return session;
    }

    private String csrfFromPage(MockHttpSession session, String url) throws Exception {
        String html = mockMvc.perform(get(url).session(session))
                .andReturn().getResponse().getContentAsString();
        return csrf(html);
    }

    private String csrf(String html) {
        Matcher matcher = CSRF.matcher(html);
        if (!matcher.find()) {
            throw new IllegalStateException("No CSRF token found in page");
        }
        return matcher.group(1);
    }

    private Long extractAddressId(String html) {
        Matcher matcher = Pattern.compile("/account/addresses/(\\d+)/edit").matcher(html);
        assertThat(matcher.find()).as("address edit link should be rendered").isTrue();
        return Long.valueOf(matcher.group(1));
    }
}
