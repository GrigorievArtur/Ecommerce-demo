package com.example.ecommercedemo.checkout;

import com.example.ecommercedemo.common.Helpers;
import com.example.ecommercedemo.dtos.carts.items.CreateItemDTO;
import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.entities.carts.Cart;
import com.example.ecommercedemo.entities.products.Product;
import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.repositories.carts.CartRepo;
import com.example.ecommercedemo.repositories.orders.OrderRepo;
import com.example.ecommercedemo.repositories.shipping.ShippingPresetsRepo;
import com.example.ecommercedemo.services.carts.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:checkout-test;MODE=MySQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class CheckoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Helpers helpers;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartService cartService;

    @Autowired
    private ShippingPresetsRepo shippingPresetsRepo;

    @Autowired
    private OrderRepo orderRepo;

    private User owner;
    private String ownerToken;
    private User otherUser;
    private String otherToken;
    private Product product;
    private Cart ownerCart;

    private final String presetName = "Home";
    private final String fullName = "John Doe";
    private final String email = "john@example.com";
    private final String phone = "123456789";
    private final String country = "US";
    private final String state = "CA";
    private final String city = "Los Angeles";
    private final String street = "123 Main St";
    private final String building = "A";
    private final String apartment = "4B";
    private final String postalCode = "90001";

    @BeforeEach
    void setUp() {
        owner = helpers.createMockUser();
        ownerToken = helpers.authenticateCreatedUser(owner);

        otherUser = helpers.createMockUser();
        otherToken = helpers.authenticateCreatedUser(otherUser);

        product = helpers.getSavedProduct();
        ownerCart = cartRepo.save(Cart.builder().user(owner).build());
    }

    // ====================================================================
    // POST /api/checkout/default
    // ====================================================================

    @Test
    void checkoutDefaultShippingPreset_success() throws Exception {
        addItemToCart(ownerCart, 2);
        createDefaultPreset(owner);

        mockMvc.perform(
                        post("/api/checkout/default")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(owner.getId().intValue()))
                .andExpect(jsonPath("$.shippingPreset.presetName").value(presetName))
                .andExpect(jsonPath("$.shippingPreset.default").value(true))
                .andExpect(jsonPath("$.items", hasSize(1)))
                // Product: grossPrice=100, qty=2 → grossAmount=200
                .andExpect(jsonPath("$.items[0].priceSnapshot.grossAmount").value(200.0))
                .andExpect(jsonPath("$.items[0].priceSnapshot.percentageDiscount").value(10));
    }

    @Test
    void checkoutDefaultShippingPreset_noDefaultPreset_throws() throws Exception {
        addItemToCart(ownerCart, 2);
        // deliberately NO default preset created

        mockMvc.perform(
                        post("/api/checkout/default")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void checkoutDefaultShippingPreset_emptyCart_succeeds() throws Exception {
        createDefaultPreset(owner);
        // cart has no items

        mockMvc.perform(
                        post("/api/checkout/default")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.shippingPreset.presetName").value(presetName));
    }

    @Test
    void checkoutDefaultShippingPreset_otherUsersDefaultNotUsed() throws Exception {
        addItemToCart(ownerCart, 2);
        // default preset for otherUser, NOT for owner
        createDefaultPreset(otherUser);

        mockMvc.perform(
                        post("/api/checkout/default")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ====================================================================
    // POST /api/checkout/saved/{shippingPresetId}
    // ====================================================================

    @Test
    void checkoutSavedShippingPreset_success() throws Exception {
        addItemToCart(ownerCart, 3);
        ShippingPreset preset = createSavedPreset(owner);

        mockMvc.perform(
                        post("/api/checkout/saved/" + preset.getId())
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(owner.getId().intValue()))
                .andExpect(jsonPath("$.shippingPreset.presetName").value("Work"))
                .andExpect(jsonPath("$.shippingPreset.fullName").value(fullName))
                .andExpect(jsonPath("$.items", hasSize(1)))
                // 3 items → grossAmount = 100 * 3 = 300
                .andExpect(jsonPath("$.items[0].priceSnapshot.grossAmount").value(300.0));
    }

    @Test
    void checkoutSavedShippingPreset_notFound() throws Exception {
        addItemToCart(ownerCart, 2);

        mockMvc.perform(
                        post("/api/checkout/saved/9999")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void checkoutSavedShippingPreset_notOwnedByUser() throws Exception {
        addItemToCart(ownerCart, 2);
        ShippingPreset preset = createSavedPreset(otherUser);

        mockMvc.perform(
                        post("/api/checkout/saved/" + preset.getId())
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void checkoutSavedShippingPreset_cartNotFound() throws Exception {
        // Create a fresh user with NO cart
        User userWithoutCart = helpers.createMockUser();
        String token = helpers.authenticateCreatedUser(userWithoutCart);
        ShippingPreset preset = createSavedPreset(userWithoutCart);
        UUID randomSuid = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/checkout/saved/" + preset.getId())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", randomSuid.toString()))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ====================================================================
    // POST /api/checkout/new
    // ====================================================================

    @Test
    void checkoutNewShippingPreset_success() throws Exception {
        addItemToCart(ownerCart, 1);
        CreateShippingPresetDTO dto = createValidShippingDto();

        mockMvc.perform(
                        post("/api/checkout/new")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(owner.getId().intValue()))
                .andExpect(jsonPath("$.shippingPreset.presetName").value(presetName))
                .andExpect(jsonPath("$.shippingPreset.fullName").value(fullName))
                .andExpect(jsonPath("$.shippingPreset.email").value(email))
                .andExpect(jsonPath("$.shippingPreset.city").value(city))
                .andExpect(jsonPath("$.items", hasSize(1)))
                // 1 item → grossAmount = 100
                .andExpect(jsonPath("$.items[0].priceSnapshot.grossAmount").value(100.0));
    }

    @Test
    void checkoutNewShippingPreset_missingPresetName() throws Exception {
        addItemToCart(ownerCart, 2);
        CreateShippingPresetDTO dto = createValidShippingDto();
        dto.setPresetName(null);

        mockMvc.perform(
                        post("/api/checkout/new")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutNewShippingPreset_validationFail_missingEmail() throws Exception {
        addItemToCart(ownerCart, 2);
        CreateShippingPresetDTO dto = createValidShippingDto();
        dto.setEmail(null);

        mockMvc.perform(
                        post("/api/checkout/new")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutNewShippingPreset_validationFail_invalidEmail() throws Exception {
        addItemToCart(ownerCart, 2);
        CreateShippingPresetDTO dto = createValidShippingDto();
        dto.setEmail("not-an-email");

        mockMvc.perform(
                        post("/api/checkout/new")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutNewShippingPreset_validationFail_missingFullName() throws Exception {
        addItemToCart(ownerCart, 2);
        CreateShippingPresetDTO dto = createValidShippingDto();
        dto.setFullName(null);

        mockMvc.perform(
                        post("/api/checkout/new")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutNewShippingPreset_validationFail_missingPhone() throws Exception {
        addItemToCart(ownerCart, 2);
        CreateShippingPresetDTO dto = createValidShippingDto();
        dto.setPhoneNumber(null);

        mockMvc.perform(
                        post("/api/checkout/new")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutNewShippingPreset_validationFail_missingIsDefault() throws Exception {
        addItemToCart(ownerCart, 2);
        CreateShippingPresetDTO dto = createValidShippingDto();
        dto.setIsDefault(null);

        mockMvc.perform(
                        post("/api/checkout/new")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ====================================================================
    // Cross-cutting concerns
    // ====================================================================

    @Test
    void checkout_requiresAuthentication() throws Exception {
        addItemToCart(ownerCart, 2);
        createDefaultPreset(owner);

        mockMvc.perform(
                        post("/api/checkout/default")
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void checkout_persistsOrderInDatabase() throws Exception {
        addItemToCart(ownerCart, 2);
        createDefaultPreset(owner);

        long orderCountBefore = orderRepo.count();

        mockMvc.perform(
                        post("/api/checkout/default")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andExpect(status().isOk());

        assertEquals(orderCountBefore + 1, orderRepo.count());
    }

    @Test
    void checkout_multipleCheckoutsCreateMultipleOrders() throws Exception {
        addItemToCart(ownerCart, 1);
        ShippingPreset preset = createSavedPreset(owner);

        long orderCountBefore = orderRepo.count();

        mockMvc.perform(
                        post("/api/checkout/saved/" + preset.getId())
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/api/checkout/saved/" + preset.getId())
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(new Cookie("suid", ownerCart.getSuid().toString()))
                )
                .andExpect(status().isOk());

        assertEquals(orderCountBefore + 2, orderRepo.count());
    }

    // ====================================================================
    // Helpers
    // ====================================================================

    private void addItemToCart(Cart cart, int quantity) {
        CreateItemDTO dto = CreateItemDTO.builder()
                .productId(product.getId())
                .quantity(quantity)
                .build();
        cartService.addItemToCart(dto, cart.getSuid());
    }

    private void createDefaultPreset(User user) {
        ShippingPreset preset = ShippingPreset.builder()
                .presetName(presetName)
                .isDefault(true)
                .fullName(fullName)
                .email(email)
                .phoneNumber(phone)
                .country(country)
                .state(state)
                .city(city)
                .street(street)
                .building(building)
                .apartment(apartment)
                .postalCode(postalCode)
                .user(user)
                .build();
        shippingPresetsRepo.save(preset);
    }

    private ShippingPreset createSavedPreset(User user) {
        ShippingPreset preset = ShippingPreset.builder()
                .presetName("Work")
                .isDefault(false)
                .fullName(fullName)
                .email(email)
                .phoneNumber(phone)
                .country(country)
                .state(state)
                .city(city)
                .street(street)
                .building(building)
                .apartment(apartment)
                .postalCode(postalCode)
                .user(user)
                .build();
        return shippingPresetsRepo.save(preset);
    }

    private CreateShippingPresetDTO createValidShippingDto() {
        CreateShippingPresetDTO dto = new CreateShippingPresetDTO();
        dto.setPresetName(presetName);
        dto.setIsDefault(false);
        dto.setFullName(fullName);
        dto.setEmail(email);
        dto.setPhoneNumber(phone);
        dto.setCountry(country);
        dto.setState(state);
        dto.setCity(city);
        dto.setStreet(street);
        dto.setBuilding(building);
        dto.setApartment(apartment);
        dto.setPostalCode(postalCode);
        return dto;
    }
}
