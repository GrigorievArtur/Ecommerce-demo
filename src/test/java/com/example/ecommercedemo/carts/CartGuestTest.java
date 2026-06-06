package com.example.ecommercedemo.carts;

import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.services.carts.CartService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
public class CartGuestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartService cartService;


    //********************************************************************
    // GET
    //********************************************************************

    @Test
    void getCart_Guest_success() throws Exception {
        var cart = cartRepo.save(new Cart());
        var suid = cart.getSuid();

        mockMvc.perform(
                        get("/api/cart")
//                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", suid.toString()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }





}
