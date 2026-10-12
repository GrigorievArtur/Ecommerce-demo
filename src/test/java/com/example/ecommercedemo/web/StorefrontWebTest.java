package com.example.ecommercedemo.web;

import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.enums.products.Category;
import com.example.ecommercedemo.models.pricing.Price;
import com.example.ecommercedemo.repositories.products.ProductRepo;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Renders the server-side pages against an in-memory database. Guards against FreeMarker
 * syntax errors, missing model attributes and broken security matchers.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:web-test;MODE=MySQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.seed.enabled=true"
})
@AutoConfigureMockMvc
class StorefrontWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Product product;

    @BeforeEach
    void setUp() {
        if (productRepo.count() == 0) {
            product = productRepo.save(Product.builder()
                    .name("Web Test Set")
                    .description("A product created for the web rendering test.")
                    .category(Category.DEMO1)
                    .stock(10)
                    .price(Price.builder()
                            .grossPrice(new BigDecimal("49.99"))
                            .percentageDiscount(new BigDecimal("10"))
                            .quantity(BigDecimal.ONE)
                            .build())
                    .build());
        } else {
            product = productRepo.findAll().get(0);
        }
    }

    @Test
    void home_renders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("New arrivals")));
    }

    @Test
    void catalog_renders_withCategoryFilter() throws Exception {
        mockMvc.perform(get("/products")
                        .param("category", "DEMO1")
                        .param("sort", "price-asc"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("All products")));
    }

    @Test
    void productDetail_renders_withStructuredData() throws Exception {
        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("application/ld+json")))
                .andExpect(content().string(containsString(product.getName())));
    }

    @Test
    void emptyCart_renders() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your cart")));
    }

    @Test
    void login_renders() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign in")));
    }

    @Test
    void robots_and_sitemap_render() throws Exception {
        mockMvc.perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sitemap:")));
        mockMvc.perform(get("/sitemap.xml"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<urlset")));
    }

    @Test
    void admin_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void productWithNullPriceColumns_stillRenders() throws Exception {
        // Simulate a legacy row whose embedded price columns are all NULL.
        jdbcTemplate.update("update product set gross_price=null, gross_amount=null, gross_discount=null, "
                + "percentage_discount=null, quantity=null where id=?", product.getId());

        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(product.getName())));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void unknownProduct_returns404() throws Exception {
        mockMvc.perform(get("/products/999999")).andExpect(status().isNotFound());
    }

    @Test
    void renderedPages_haveNoNewlinesInsideAttributes_andUseCurrencySymbol() throws Exception {
        String html = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertFalse(Pattern.compile("=\"[^\"]*\\n[^\"]*\"").matcher(html).find(),
                "An HTML attribute value contains a newline");
        assertTrue(html.contains("class=\"price-now\">$"), "Prices should use the currency symbol");
    }

    @Test
    void errorPage_rendersWithSiteLayoutAndStatus() throws Exception {
        mockMvc.perform(get("/error")
                        .accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/missing-page"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Page not found")))
                .andExpect(content().string(containsString("class=\"site-header\"")));
    }
}
