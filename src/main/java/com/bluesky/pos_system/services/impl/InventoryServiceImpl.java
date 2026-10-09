package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.InventoryTransactionType;
import com.bluesky.pos_system.mappers.InventoryMapper;
import com.bluesky.pos_system.mappers.InventoryTransactionMapper;
import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Inventory;
import com.bluesky.pos_system.models.InventoryTransaction;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.InventoryDTO;
import com.bluesky.pos_system.payload.dto.InventoryTransactionDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.payload.dto.StockAdjustmentRequest;
import com.bluesky.pos_system.repositories.BranchRepository;
import com.bluesky.pos_system.repositories.InventoryRepository;
import com.bluesky.pos_system.repositories.InventoryTransactionRepository;
import com.bluesky.pos_system.repositories.ProductRepository;
import com.bluesky.pos_system.services.InventoryService;
import com.bluesky.pos_system.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryServiceImpl implements InventoryService {
    InventoryRepository inventoryRepository;
    BranchRepository branchRepository;
    ProductRepository productRepository;
    InventoryTransactionRepository inventoryTransactionRepository;
    UserService userService;

    @Override
    @Transactional
    public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
        Branch branch = branchRepository.findById(inventoryDTO.getBranchId()).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy chi nhánh với id: " + inventoryDTO.getBranchId())
        );
        Product product = productRepository.findById(inventoryDTO.getProductId()).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy sản phẩm với id: " + inventoryDTO.getProductId())
        );

        // If inventory already exists for this branch & product, update quantity instead of duplicate
        Inventory existing = inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId());
        if (existing != null) {
            existing.setQuantity(inventoryDTO.getQuantity());
            return InventoryMapper.toDTO(inventoryRepository.save(existing));
        }

        Inventory inventory = InventoryMapper.toEntity(inventoryDTO, branch, product);
        return InventoryMapper.toDTO(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryDTO updateInventory(UUID id, InventoryDTO inventoryDTO) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy thông tin kho hàng")
        );
        inventory.setQuantity(inventoryDTO.getQuantity());

        return InventoryMapper.toDTO(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryDTO adjustStock(UUID branchId, UUID productId, Integer deltaQuantity) {
        StockAdjustmentRequest req = StockAdjustmentRequest.builder()
                .branchId(branchId)
                .productId(productId)
                .quantityChange(deltaQuantity != null ? deltaQuantity : 0)
                .type(deltaQuantity != null && deltaQuantity >= 0 ? InventoryTransactionType.ADJUSTMENT : InventoryTransactionType.STOCK_OUT)
                .reason("MANUAL_ADJUSTMENT")
                .notes("Điều chỉnh tồn kho qua API nhanh")
                .build();
        return adjustStock(req, null);
    }

    @Override
    @Transactional
    public InventoryDTO adjustStock(StockAdjustmentRequest request, User user) {
        Branch branch = branchRepository.findById(request.getBranchId()).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy chi nhánh")
        );
        Product product = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy sản phẩm")
        );

        User currentUser = user;
        if (currentUser == null) {
            try {
                currentUser = userService.getCurrentUser();
            } catch (Exception ignored) {
            }
        }

        Inventory inventory = inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId());
        int prevQty = 0;
        if (inventory == null) {
            inventory = Inventory.builder()
                    .branch(branch)
                    .product(product)
                    .quantity(Math.max(0, request.getQuantityChange()))
                    .build();
        } else {
            prevQty = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
            int newQty = Math.max(0, prevQty + request.getQuantityChange());
            inventory.setQuantity(newQty);
        }

        Inventory savedInventory = inventoryRepository.save(inventory);

        // Record stock audit trail transaction
        InventoryTransactionType txType = request.getType() != null
                ? request.getType()
                : (request.getQuantityChange() >= 0 ? InventoryTransactionType.ADJUSTMENT : InventoryTransactionType.STOCK_OUT);

        String refNum = "ADJ-" + System.currentTimeMillis() % 1000000;
        String notes = request.getReason() != null
                ? (request.getReason() + (request.getNotes() != null ? " - " + request.getNotes() : ""))
                : request.getNotes();

        InventoryTransaction tx = InventoryTransaction.builder()
                .branch(branch)
                .product(product)
                .type(txType)
                .quantityChange(request.getQuantityChange())
                .balanceAfter(savedInventory.getQuantity())
                .referenceNumber(refNum)
                .notes(notes)
                .createdBy(currentUser)
                .build();
        inventoryTransactionRepository.save(tx);

        return InventoryMapper.toDTO(savedInventory);
    }

    @Override
    @Transactional
    public void deleteInventory(UUID id) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy thông tin kho hàng")
        );
        inventoryRepository.delete(inventory);
    }

    @Override
    public InventoryDTO getInventoryById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy thông tin kho hàng")
        );
        return InventoryMapper.toDTO(inventory);
    }

    @Override
    public InventoryDTO getInventoryByProductIdAndBranchId(UUID productId, UUID branchId) {
        Inventory inventory = inventoryRepository.findByProductIdAndBranchId(productId, branchId);
        return InventoryMapper.toDTO(inventory);
    }

    @Override
    public List<InventoryDTO> getAllInventoryByBranchId(UUID branchId) {
        List<Inventory> inventories = inventoryRepository.findByBranchId(branchId);
        return inventories.stream().map(InventoryMapper::toDTO).toList();
    }

    @Override
    public List<InventoryDTO> getLowStockByBranchId(UUID branchId) {
        return inventoryRepository.findLowStockByBranchId(branchId).stream()
                .map(InventoryMapper::toDTO)
                .toList();
    }

    @Override
    public PageResponse<InventoryTransactionDTO> getTransactionsByBranch(UUID branchId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InventoryTransaction> pageResult = inventoryTransactionRepository.findByBranchIdOrderByCreatedAtDesc(branchId, pageRequest);
        List<InventoryTransactionDTO> dtoList = pageResult.getContent().stream()
                .map(InventoryTransactionMapper::toDTO)
                .toList();

        return PageResponse.<InventoryTransactionDTO>builder()
                .content(dtoList)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .build();
    }

    @Override
    public List<InventoryTransactionDTO> getTransactionsByProduct(UUID productId) {
        return inventoryTransactionRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(InventoryTransactionMapper::toDTO)
                .toList();
    }

    @Override
    public Map<String, Object> getLowStockSummary(UUID branchId) {
        List<Inventory> inventories = inventoryRepository.findByBranchId(branchId);
        int totalItems = inventories.size();
        int totalUnits = inventories.stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();

        List<InventoryDTO> lowStockItems = inventories.stream()
                .filter(inv -> {
                    int min = (inv.getProduct() != null && inv.getProduct().getMinStockLevel() != null)
                            ? inv.getProduct().getMinStockLevel() : 5;
                    int q = inv.getQuantity() != null ? inv.getQuantity() : 0;
                    return q > 0 && q <= min;
                })
                .map(InventoryMapper::toDTO)
                .toList();

        List<InventoryDTO> outOfStockItems = inventories.stream()
                .filter(inv -> inv.getQuantity() == null || inv.getQuantity() <= 0)
                .map(InventoryMapper::toDTO)
                .toList();

        Map<String, Object> summary = new HashMap<>();
        summary.put("branchId", branchId);
        summary.put("totalItems", totalItems);
        summary.put("totalUnits", totalUnits);
        summary.put("lowStockCount", lowStockItems.size());
        summary.put("outOfStockCount", outOfStockItems.size());
        summary.put("lowStockItems", lowStockItems);
        summary.put("outOfStockItems", outOfStockItems);
        return summary;
    }
}
