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
                () -> new RuntimeException("Không tìm thấy")
        );
        Product product = productRepository.findById(inventoryDTO.getProductId()).orElseThrow(
                () -> new RuntimeException("Không tìm thấy sản phẩm")
        );
        Inventory inventory = InventoryMapper.toEntity(inventoryDTO, branch, product);

        return InventoryMapper.toDTO(inventoryRepository.save(inventory));
    }

    @Override
    public InventoryDTO updateInventory(UUID id, InventoryDTO inventoryDTO) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Không tìm thấy kho")
        );
        inventory.setQuantity(inventoryDTO.getQuantity());

        return InventoryMapper.toDTO(inventoryRepository.save(inventory));
    }

    @Override
    public void deleteInventory(UUID id) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Không tìm thấy kho")
        );
        inventoryRepository.delete(inventory);
    }

    @Override
    public InventoryDTO getInventoryById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Không tìm thấy kho")
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
}
