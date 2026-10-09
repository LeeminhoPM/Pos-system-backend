package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.InventoryTransactionType;
import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Inventory;
import com.bluesky.pos_system.models.InventoryTransaction;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.InventoryDTO;
import com.bluesky.pos_system.payload.dto.StockAdjustmentRequest;
import com.bluesky.pos_system.repositories.BranchRepository;
import com.bluesky.pos_system.repositories.InventoryRepository;
import com.bluesky.pos_system.repositories.InventoryTransactionRepository;
import com.bluesky.pos_system.repositories.ProductRepository;
import com.bluesky.pos_system.services.UserService;
import com.bluesky.pos_system.services.impl.InventoryServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Branch branch;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        branch = Branch.builder()
                .id(UUID.randomUUID())
                .name("Chi nhánh Hà Nội")
                .build();

        product = Product.builder()
                .id(UUID.randomUUID())
                .name("Cà phê Arabica")
                .sku("CF-001")
                .minStockLevel(10)
                .sellingPrice(45000.0)
                .costPrice(25000.0)
                .build();

        inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .branch(branch)
                .product(product)
                .quantity(50)
                .build();
    }

    @Test
    @DisplayName("Tạo mới kho hàng cho sản phẩm chưa tồn tại")
    void testCreateInventory_NewProduct() {
        InventoryDTO dto = InventoryDTO.builder()
                .branchId(branch.getId())
                .productId(product.getId())
                .quantity(30)
                .build();

        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId())).thenReturn(null);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            inv.setId(UUID.randomUUID());
            return inv;
        });

        InventoryDTO result = inventoryService.createInventory(dto);

        assertNotNull(result);
        assertEquals(30, result.getQuantity());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Cập nhật số lượng nếu sản phẩm đã tồn tại trong kho của chi nhánh")
    void testCreateInventory_ExistingProduct_UpdatesQuantity() {
        InventoryDTO dto = InventoryDTO.builder()
                .branchId(branch.getId())
                .productId(product.getId())
                .quantity(80)
                .build();

        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId())).thenReturn(inventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryDTO result = inventoryService.createInventory(dto);

        assertNotNull(result);
        assertEquals(80, inventory.getQuantity());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Điều chỉnh tăng tồn kho (Adjustment Stock In)")
    void testAdjustStock_Increase() {
        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                .branchId(branch.getId())
                .productId(product.getId())
                .quantityChange(20)
                .type(InventoryTransactionType.ADJUSTMENT)
                .reason("Kiểm kê thực tế tăng")
                .build();

        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId())).thenReturn(inventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryDTO result = inventoryService.adjustStock(request, null);

        assertNotNull(result);
        assertEquals(70, inventory.getQuantity());
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransaction.class));
    }

    @Test
    @DisplayName("Điều chỉnh giảm tồn kho (Stock Out)")
    void testAdjustStock_Decrease() {
        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                .branchId(branch.getId())
                .productId(product.getId())
                .quantityChange(-15)
                .type(InventoryTransactionType.STOCK_OUT)
                .reason("Hàng hỏng hết hạn")
                .build();

        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId())).thenReturn(inventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryDTO result = inventoryService.adjustStock(request, null);

        assertNotNull(result);
        assertEquals(35, inventory.getQuantity());
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransaction.class));
    }

    @Test
    @DisplayName("Lấy danh sách tồn kho theo chi nhánh")
    void testGetAllInventoryByBranchId() {
        when(inventoryRepository.findByBranchId(branch.getId())).thenReturn(List.of(inventory));

        List<InventoryDTO> list = inventoryService.getAllInventoryByBranchId(branch.getId());

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(product.getId(), list.get(0).getProductId());
    }

    @Test
    @DisplayName("Thống kê tồn kho thấp và hết hàng chính xác")
    void testGetLowStockSummary() {
        Inventory lowStockItem = Inventory.builder()
                .id(UUID.randomUUID())
                .branch(branch)
                .product(Product.builder().id(UUID.randomUUID()).minStockLevel(10).build())
                .quantity(3)
                .build();

        Inventory outOfStockItem = Inventory.builder()
                .id(UUID.randomUUID())
                .branch(branch)
                .product(Product.builder().id(UUID.randomUUID()).minStockLevel(5).build())
                .quantity(0)
                .build();

        when(inventoryRepository.findByBranchId(branch.getId())).thenReturn(List.of(inventory, lowStockItem, outOfStockItem));

        Map<String, Object> summary = inventoryService.getLowStockSummary(branch.getId());

        assertNotNull(summary);
        assertEquals(3, summary.get("totalItems"));
        assertEquals(53, summary.get("totalUnits")); // 50 + 3 + 0
        assertEquals(1, summary.get("lowStockCount"));
        assertEquals(1, summary.get("outOfStockCount"));
    }

    @Test
    @DisplayName("Ném ngoại lệ EntityNotFoundException khi xóa kho hàng không tồn tại")
    void testDeleteInventory_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(inventoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> inventoryService.deleteInventory(nonExistentId));
    }
}
