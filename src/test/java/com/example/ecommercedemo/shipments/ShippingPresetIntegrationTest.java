package com.example.ecommercedemo.shipments;

import com.example.ecommercedemo.common.Helpers;
import com.example.ecommercedemo.dtos.shipments.CreateShippingPresetDTO;
import com.example.ecommercedemo.dtos.shipments.UpdateShippingPresetDTO;
import com.example.ecommercedemo.entities.shipments.ShippingPreset;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.repositories.shipping.ShippingPresetsRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:shipping-test;MODE=MySQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
public class ShippingPresetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Helpers helpers;

    @Autowired
    private ShippingPresetsRepo shippingPresetsRepo;

    private User owner;
    private String ownerToken;
    private User otherUser;
    private String otherToken;

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
        // Create two users and get their tokens
        owner = helpers.createMockUser();
        ownerToken = helpers.authenticateCreatedUser(owner);

        otherUser = helpers.createMockUser();
        otherToken = helpers.authenticateCreatedUser(otherUser);
    }

    // ---------- POST /api/shipping ----------

    @Test
    void saveShippingPreset_success() throws Exception {
        CreateShippingPresetDTO dto = createValidCreateDto();

        mockMvc.perform(post("/api/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presetName").value(presetName))
                .andExpect(jsonPath("$.fullName").value(fullName))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.isDefault").value(false));
    }

    @Test
    void saveShippingPreset_validationFail_missingPresetName() throws Exception {
        CreateShippingPresetDTO dto = createValidCreateDto();
        dto.setPresetName(null);

        mockMvc.perform(post("/api/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveShippingPreset_validationFail_missingEmail() throws Exception {
        CreateShippingPresetDTO dto = createValidCreateDto();
        dto.setEmail(null);

        mockMvc.perform(post("/api/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveShippingPreset_validationFail_invalidEmail() throws Exception {
        CreateShippingPresetDTO dto = createValidCreateDto();
        dto.setEmail("invalid-email");

        mockMvc.perform(post("/api/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveShippingPreset_validationFail_missingPhone() throws Exception {
        CreateShippingPresetDTO dto = createValidCreateDto();
        dto.setPhoneNumber(null);

        mockMvc.perform(post("/api/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /api/shipping?id=... ----------

    @Test
    void getShippingPreset_success() throws Exception {
        // Create a preset for owner
        ShippingPreset preset = createPreset(owner);

        mockMvc.perform(get("/api/shipping")
                        .param("id", preset.getId().toString())
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(preset.getId()))
                .andExpect(jsonPath("$.presetName").value(preset.getPresetName()));
    }

    @Test
    void getShippingPreset_notFound() throws Exception {
        mockMvc.perform(get("/api/shipping")
                        .param("id", "999")
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getShippingPreset_notOwnedByUser() throws Exception {
        // Create a preset for owner
        ShippingPreset preset = createPreset(owner);

        // Try to access with other user
        mockMvc.perform(get("/api/shipping")
                        .param("id", preset.getId().toString())
                        .header("Authorization", "Bearer " + otherToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /api/shipping?id=... ----------

    @Test
    void updateShippingPreset_success() throws Exception {
        ShippingPreset preset = createPreset(owner);

        UpdateShippingPresetDTO dto = new UpdateShippingPresetDTO();
        dto.setPresetName("Work");
        dto.setIsDefault(true);

        mockMvc.perform(patch("/api/shipping")
                        .param("id", preset.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presetName").value("Work"))
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void updateShippingPreset_partialUpdate() throws Exception {
        ShippingPreset preset = createPreset(owner);

        UpdateShippingPresetDTO dto = new UpdateShippingPresetDTO();
        dto.setCity("San Francisco");

        mockMvc.perform(patch("/api/shipping")
                        .param("id", preset.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("San Francisco"))
                .andExpect(jsonPath("$.presetName").value(presetName)); // unchanged
    }

    @Test
    void updateShippingPreset_notFound() throws Exception {
        UpdateShippingPresetDTO dto = new UpdateShippingPresetDTO();
        dto.setPresetName("Work");

        mockMvc.perform(patch("/api/shipping")
                        .param("id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void updateShippingPreset_notOwnedByUser() throws Exception {
        ShippingPreset preset = createPreset(owner);

        UpdateShippingPresetDTO dto = new UpdateShippingPresetDTO();
        dto.setPresetName("Work");

        mockMvc.perform(patch("/api/shipping")
                        .param("id", preset.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + otherToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/shipping/paged ----------

    @Test
    void getShippingPresetPaged_success() throws Exception {
        // Create multiple presets for owner
        for (int i = 0; i < 3; i++) {
            createPreset(owner);
        }

        mockMvc.perform(get("/api/shipping/paged")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void getShippingPresetPaged_emptyPage() throws Exception {
        // No presets for owner
        mockMvc.perform(get("/api/shipping/paged")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getShippingPresetPaged_onlyOwnPresets() throws Exception {
        // Create presets for both users
        createPreset(owner);
        createPreset(owner);
        createPreset(otherUser);

        mockMvc.perform(get("/api/shipping/paged")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    // ---------- DELETE /api/shipping?id=... ----------

    @Test
    void deleteShippingPreset_success() throws Exception {
        ShippingPreset preset = createPreset(owner);

        mockMvc.perform(delete("/api/shipping")
                        .param("id", preset.getId().toString())
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void deleteShippingPreset_notFound() throws Exception {
        mockMvc.perform(delete("/api/shipping")
                        .param("id", "999")
                        .header("Authorization", "Bearer " + ownerToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteShippingPreset_notOwnedByUser() throws Exception {
        ShippingPreset preset = createPreset(owner);

        mockMvc.perform(delete("/api/shipping")
                        .param("id", preset.getId().toString())
                        .header("Authorization", "Bearer " + otherToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ---------- Helpers ----------

    private CreateShippingPresetDTO createValidCreateDto() {
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

    private ShippingPreset createPreset(User user) {
        ShippingPreset preset = ShippingPreset.builder()
                .presetName(presetName)
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
}