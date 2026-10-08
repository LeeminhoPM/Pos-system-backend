package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.InventoryTransactionType;
import com.bluesky.pos_system.domains.StockReceiptType;
import com.bluesky.pos_system.mappers.StockReceiptMapper;
import com.bluesky.pos_system.models.*;
import com.bluesky.pos_system.payload.dto.InventoryTransactionDTO;
import com.bluesky.pos_system.payload.dto.StockReceiptDTO;
import com.bluesky.pos_system.payload.dto.StockReceiptItemDTO;
import com.bluesky.pos_system.repositories.*;
import com.bluesky.pos_system.services.StockReceiptService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockReceiptServiceImpl implements StockReceiptService {
    StockReceiptRepository stockReceiptRepository;
    BranchRepository branchRepository;
    SupplierRepository supplierRepository;
    ProductRepository productRepository;
    InventoryRepository inventoryRepository;
    InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    @Transactional
    public StockReceiptDTO createStockReceipt(StockReceiptDTO dto, User user) {
        Branch branch = branchRepository.findById(dto.getBranchId()).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy chi nhánh")
        );

        Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepository.findById(dto.getSupplierId()).orElse(null);
        }

        String prefix = dto.getType() == StockReceiptType.STOCK_IN ? "IN-" : "OUT-";
        String receiptNumber = prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", (int)(Math.random() * 10000));

        StockReceipt receipt = StockReceipt.builder()
                .receiptNumber(receiptNumber)
                .type(dto.getType() != null ? dto.getType() : StockReceiptType.STOCK_IN)
                .branch(branch)
                .supplier(supplier)
                .createdBy(user)
                .notes(dto.getNotes())
                .build();

        List<StockReceiptItem> items = new ArrayList<>();
        double totalAmount = 0.0;

        for (StockReceiptItemDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId()).orElseThrow(
                    () -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + itemDTO.getProductId())
            );

            int qty = itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 1;
            double cost = itemDTO.getUnitCost() != null ? itemDTO.getUnitCost()
                    : (product.getCostPrice() != null ? product.getCostPrice() : 0.0);
            double lineTotal = cost * qty;
            totalAmount += lineTotal;

            StockReceiptItem item = StockReceiptItem.builder()
                    .stockReceipt(receipt)
                    .product(product)
                    .quantity(qty)
                    .unitCost(cost)
                    .totalPrice(lineTotal)
                    .build();
            items.add(item);

            // Update Branch Inventory
            Inventory inventory = inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId());
            if (inventory == null) {
                inventory = Inventory.builder()
                        .product(product)
                        .branch(branch)
                        .quantity(0)
                        .build();
            }

            int change = dto.getType() == StockReceiptType.STOCK_IN ? qty : -qty;
            int newBalance = Math.max(0, inventory.getQuantity() + change);
            inventory.setQuantity(newBalance);
            inventoryRepository.save(inventory);

            // Log Inventory Transaction
            InventoryTransactionType txType = dto.getType() == StockReceiptType.STOCK_IN
                    ? InventoryTransactionType.STOCK_IN
                    : InventoryTransactionType.STOCK_OUT;

            InventoryTransaction tx = InventoryTransaction.builder()
                    .branch(branch)
                    .product(product)
                    .type(txType)
                    .quantityChange(change)
                    .balanceAfter(newBalance)
                    .referenceNumber(receiptNumber)
                    .notes(dto.getNotes())
                    .createdBy(user)
                    .build();
            inventoryTransactionRepository.save(tx);
        }

        receipt.setItems(items);
        receipt.setTotalAmount(totalAmount);

        StockReceipt savedReceipt = stockReceiptRepository.save(receipt);
        log.info("Stock receipt processed: receiptNumber={}, type={}, branchId={}, itemsCount={}, totalAmount={}",
                savedReceipt.getReceiptNumber(), savedReceipt.getType(), branch.getId(), items.size(), totalAmount);
        return StockReceiptMapper.toDTO(savedReceipt);
    }

    @Override
    public StockReceiptDTO getStockReceiptById(UUID id) {
        StockReceipt receipt = stockReceiptRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy phiếu kho")
        );
        return StockReceiptMapper.toDTO(receipt);
    }

    @Override
    public List<StockReceiptDTO> getReceiptsByBranch(UUID branchId) {
        return stockReceiptRepository.findByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .map(StockReceiptMapper::toDTO)
                .toList();
    }

    @Override
    public List<InventoryTransactionDTO> getInventoryHistory(UUID branchId) {
        return inventoryTransactionRepository.findByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .map(tx -> InventoryTransactionDTO.builder()
                        .id(tx.getId())
                        .branchId(tx.getBranch().getId())
                        .branchName(tx.getBranch().getName())
                        .productId(tx.getProduct().getId())
                        .productName(tx.getProduct().getName())
                        .sku(tx.getProduct().getSku())
                        .type(tx.getType())
                        .quantityChange(tx.getQuantityChange())
                        .balanceAfter(tx.getBalanceAfter())
                        .referenceNumber(tx.getReferenceNumber())
                        .notes(tx.getNotes())
                        .createdByName(tx.getCreatedBy() != null ? tx.getCreatedBy().getName() : null)
                        .createdAt(tx.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public List<InventoryTransactionDTO> getProductInventoryHistory(UUID productId) {
        return inventoryTransactionRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(tx -> InventoryTransactionDTO.builder()
                        .id(tx.getId())
                        .branchId(tx.getBranch().getId())
                        .branchName(tx.getBranch().getName())
                        .productId(tx.getProduct().getId())
                        .productName(tx.getProduct().getName())
                        .sku(tx.getProduct().getSku())
                        .type(tx.getType())
                        .quantityChange(tx.getQuantityChange())
                        .balanceAfter(tx.getBalanceAfter())
                        .referenceNumber(tx.getReferenceNumber())
                        .notes(tx.getNotes())
                        .createdByName(tx.getCreatedBy() != null ? tx.getCreatedBy().getName() : null)
                        .createdAt(tx.getCreatedAt())
                        .build())
                .toList();
    }
}
