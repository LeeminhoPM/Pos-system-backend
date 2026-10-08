package com.bluesky.pos_system;

import com.bluesky.pos_system.models.Promotion;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.payload.dto.ApplyPromotionRequestDTO;
import com.bluesky.pos_system.repositories.PromotionRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Store store;
    private Promotion promotion;

    @BeforeEach
    void setUp() {
        promotionRepository.deleteAll();

        store = storeRepository.save(TestDataFactory.newStore());
        promotion = promotionRepository.save(TestDataFactory.newPromotion(store));
    }

    @Test
    @DisplayName("POST /api/promotions/apply - Valid Code Returns 200 OK & Calculated Discount")
    @WithMockUser(username = "admin@pos.com")
    void testApplyPromotion_Valid() throws Exception {
        ApplyPromotionRequestDTO request = ApplyPromotionRequestDTO.builder()
                .code("SALE10")
                .orderAmount(200000.0)
                .build();

        mockMvc.perform(post("/api/promotions/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.code", is("SALE10")))
                .andExpect(jsonPath("$.discountAmount", is(20000.0)))
                .andExpect(jsonPath("$.finalAmount", is(180000.0)));
    }

    @Test
    @DisplayName("POST /api/promotions/apply - Nonexistent Code Returns valid=false")
    @WithMockUser(username = "admin@pos.com")
    void testApplyPromotion_Nonexistent() throws Exception {
        ApplyPromotionRequestDTO request = ApplyPromotionRequestDTO.builder()
                .code("INVALID999")
                .orderAmount(200000.0)
                .build();

        mockMvc.perform(post("/api/promotions/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.discountAmount", is(0.0)));
    }
}
