package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.domains.StockReceiptType;
import com.bluesky.pos_system.models.StockReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockReceiptRepository extends JpaRepository<StockReceipt, UUID> {
    List<StockReceipt> findByBranchIdOrderByCreatedAtDesc(UUID branchId);

    Page<StockReceipt> findByBranchIdOrderByCreatedAtDesc(UUID branchId, Pageable pageable);

    Optional<StockReceipt> findByReceiptNumber(String receiptNumber);

    List<StockReceipt> findByBranchIdAndTypeOrderByCreatedAtDesc(UUID branchId, StockReceiptType type);

    List<StockReceipt> findByBranchIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID branchId, LocalDateTime start, LocalDateTime end
    );
}
