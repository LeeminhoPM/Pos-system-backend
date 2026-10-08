package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.mappers.InventoryMapper;
import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Inventory;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.payload.dto.InventoryDTO;
import com.bluesky.pos_system.repositories.BranchRepository;
import com.bluesky.pos_system.repositories.InventoryRepository;
import com.bluesky.pos_system.repositories.ProductRepository;
import com.bluesky.pos_system.services.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryServiceImpl implements InventoryService {
    InventoryRepository inventoryRepository;
    BranchRepository branchRepository;
    ProductRepository productRepository;

    @Override
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
    public InventoryDTO updateInventory(UUID id, InventoryDTO inventoryDTO) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy thông tin kho hàng")
        );
        inventory.setQuantity(inventoryDTO.getQuantity());

        return InventoryMapper.toDTO(inventoryRepository.save(inventory));
    }

    @Override
    public InventoryDTO adjustStock(UUID branchId, UUID productId, Integer deltaQuantity) {
        Branch branch = branchRepository.findById(branchId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy chi nhánh")
        );
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy sản phẩm")
        );

        Inventory inventory = inventoryRepository.findByProductIdAndBranchId(productId, branchId);
        if (inventory == null) {
            inventory = Inventory.builder()
                    .branch(branch)
                    .product(product)
                    .quantity(Math.max(0, deltaQuantity != null ? deltaQuantity : 0))
                    .build();
        } else {
            int newQty = Math.max(0, inventory.getQuantity() + (deltaQuantity != null ? deltaQuantity : 0));
            inventory.setQuantity(newQty);
        }

        return InventoryMapper.toDTO(inventoryRepository.save(inventory));
    }

    @Override
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
        List<Inventory> inventories = inventoryRepository.findByBranchId(branchId);
        return inventories.stream()
                .filter(inv -> {
                    int minStock = (inv.getProduct() != null && inv.getProduct().getMinStockLevel() != null)
                            ? inv.getProduct().getMinStockLevel()
                            : 5;
                    return inv.getQuantity() <= minStock;
                })
                .map(InventoryMapper::toDTO)
                .toList();
    }
}
