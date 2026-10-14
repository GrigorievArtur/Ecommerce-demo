package com.example.ecommercedemo.carts;

import com.example.ecommercedemo.common.Helpers;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.entities.products.Product;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Locks in the cart-API contract: reading never creates, creation is explicit and
 * exclusive, and guest carts are independent from account carts.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:products-test;MODE=MySQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class CartLifecycleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Helpers helpers;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCart_withNoCart_returns404() throws Exception {
        mockMvc.perform(get("/api/cart").cookie(new Cookie("suid", UUID.randomUUID().toString())))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCart_isRetrievableAndExclusive() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/cart").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("suid"))
                .andReturn();
        Cookie suid = created.getResponse().getCookie("suid");
        assertThat(suid).isNotNull();

        mockMvc.perform(get("/api/cart").cookie(suid))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart").contentType(MediaType.APPLICATION_JSON).content("{}").cookie(suid))
                .andExpect(status().isConflict());
    }

    @Test
    void createCart_forAccountIsExclusive() throws Exception {
        String token = helpers.authenticateCreatedUser();
        mockMvc.perform(post("/api/cart").contentType(MediaType.APPLICATION_JSON).content("{}").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/cart").contentType(MediaType.APPLICATION_JSON).content("{}").header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void guestAndAccountCarts_areIndependent() throws Exception {
        String token = helpers.authenticateCreatedUser();
        Cookie guestCookie = mockMvc.perform(post("/api/cart").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getCookie("suid");
        assertThat(guestCookie).isNotNull();

        mockMvc.perform(post("/api/cart").contentType(MediaType.APPLICATION_JSON).content("{}").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cart").cookie(guestCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(guestCookie.getValue())));
        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suid").value(not(guestCookie.getValue())));
    }

    @Test
    void addItem_withoutCart_startsGuestCartAndSetsCookie() throws Exception {
        Product product = helpers.getSavedProduct();
        CreateItemDTO dto = CreateItemDTO.builder().productId(product.getId()).quantity(1).build();

        MvcResult result = mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("suid"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andReturn();

        Cookie suid = result.getResponse().getCookie("suid");
        mockMvc.perform(get("/api/cart").cookie(suid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));
    }
}
