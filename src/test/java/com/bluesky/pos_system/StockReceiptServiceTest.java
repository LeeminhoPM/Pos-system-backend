package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.InventoryTransactionType;
import com.bluesky.pos_system.domains.StockReceiptType;
import com.bluesky.pos_system.models.*;
import com.bluesky.pos_system.payload.dto.StockReceiptDTO;
import com.bluesky.pos_system.payload.dto.StockReceiptItemDTO;
import com.bluesky.pos_system.repositories.*;
import com.bluesky.pos_system.services.impl.StockReceiptServiceImpl;
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
class StockReceiptServiceTest {

    @Mock
    private StockReceiptRepository stockReceiptRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @InjectMocks
    private StockReceiptServiceImpl stockReceiptService;

    private Store store;
    private Branch branch;
    private Supplier supplier;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        store = TestDataFactory.createStore();
        branch = TestDataFactory.createBranch(store);
        supplier = TestDataFactory.createSupplier(store);
        Category category = TestDataFactory.createCategory(store);
        product = TestDataFactory.createProduct(store, category, supplier);

        inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .product(product)
                .branch(branch)
                .quantity(10)
                .build();
    }

    @Test
    @DisplayName("Create Stock-In Receipt - Increases Inventory and Records Movement")
    void testCreateStockInReceipt_Success() {
        StockReceiptItemDTO itemDTO = StockReceiptItemDTO.builder()
                .productId(product.getId())
                .quantity(20)
                .unitCost(15000.0)
                .build();

        StockReceiptDTO request = StockReceiptDTO.builder()
                .branchId(branch.getId())
                .supplierId(supplier.getId())
                .type(StockReceiptType.STOCK_IN)
                .notes("Nhập thêm 20 gói cà phê")
                .items(List.of(itemDTO))
                .build();

        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId())).thenReturn(inventory);
        when(stockReceiptRepository.save(any(StockReceipt.class))).thenAnswer(inv -> {
            StockReceipt r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        User user = User.builder().fullName("Warehouse Manager").build();
        StockReceiptDTO result = stockReceiptService.createStockReceipt(request, user);

        assertNotNull(result);
        assertEquals(300000.0, result.getTotalAmount()); // 20 * 15k
        assertTrue(result.getReceiptNumber().startsWith("IN-"));

        // Verify inventory balance increased: 10 + 20 = 30
        assertEquals(30, inventory.getQuantity());
        verify(inventoryRepository, times(1)).save(inventory);

        // Verify inventory transaction record was saved
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransaction.class));
    }
}
