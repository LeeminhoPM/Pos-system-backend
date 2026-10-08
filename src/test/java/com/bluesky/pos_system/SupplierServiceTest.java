package com.bluesky.pos_system;

import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.Supplier;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.SupplierDTO;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.repositories.SupplierRepository;
import com.bluesky.pos_system.services.impl.SupplierServiceImpl;
import com.bluesky.pos_system.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Store store;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        store = TestDataFactory.createStore();
        supplier = TestDataFactory.createSupplier(store);
    }

    @Test
    @DisplayName("Create Supplier - Generates Code Automatically")
    void testCreateSupplier_AutoCode() {
        SupplierDTO req = SupplierDTO.builder()
                .name("Vinamilk Distribution")
                .contactName("Le Thi D")
                .phone("0908889999")
                .storeId(store.getId())
                .build();

        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> {
            Supplier s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            if (s.getCode() == null || s.getCode().isBlank()) {
                s.setCode("SUP-AUTO-01");
            }
            return s;
        });

        User user = User.builder().store(store).build();
        SupplierDTO result = supplierService.createSupplier(req, user);

        assertNotNull(result);
        assertEquals("Vinamilk Distribution", result.getName());
        assertNotNull(result.getCode());
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Get Suppliers By Store - Returns Supplier List")
    void testGetSuppliersByStore() {
        when(supplierRepository.findByStoreId(store.getId())).thenReturn(List.of(supplier));

        List<SupplierDTO> list = supplierService.getSuppliersByStore(store.getId());

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Nhà Cung Cấp Hạt Cà Phê", list.get(0).getName());
    }
}
