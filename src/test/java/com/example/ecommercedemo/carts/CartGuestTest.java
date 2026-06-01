package com.example.ecommercedemo.carts;

import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.services.carts.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

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
//GET
//    void getCart_Guest_success() throws Exception {
//        var cart = cartService.getCart()
//    }

}
