package com.bluesky.pos_system;

import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.repositories.BranchRepository;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Branch branch;

    @BeforeEach
    void setUp() {
        Store store = storeRepository.findAll().stream().findFirst().orElseGet(() ->
                storeRepository.save(TestDataFactory.newStore())
        );

        branch = branchRepository.findAll().stream().findFirst().orElseGet(() -> {
            Branch b = Branch.builder()
                    .name("Test Branch for Orders")
                    .store(store)
                    .build();
            return branchRepository.save(b);
        });
    }

    @Test
    @WithMockUser(username = "admin@pos.com", roles = {"ADMIN"})
    @DisplayName("GET /api/orders/branch/{branchId} trả về 200 OK với danh sách đơn hàng")
    void testGetOrdersByBranch_Success() throws Exception {
        mockMvc.perform(get("/api/orders/branch/" + branch.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "cashier@pos.com", roles = {"CASHIER"})
    @DisplayName("GET /api/orders/recent/branch/{branchId} trả về 200 OK với các đơn hàng gần nhất")
    void testGetRecentOrdersByBranch_Success() throws Exception {
        mockMvc.perform(get("/api/orders/recent/branch/" + branch.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/orders/branch/{branchId} không có token trả về 401 hoặc 403")
    void testGetOrdersByBranch_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/orders/branch/" + branch.getId()))
                .andExpect(status().isForbidden());
    }
}
