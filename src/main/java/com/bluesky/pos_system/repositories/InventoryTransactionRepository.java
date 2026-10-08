package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.domains.InventoryTransactionType;
import com.bluesky.pos_system.models.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    List<InventoryTransaction> findByBranchIdOrderByCreatedAtDesc(UUID branchId);

    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(UUID productId);

    Page<InventoryTransaction> findByBranchIdOrderByCreatedAtDesc(UUID branchId, Pageable pageable);

    List<InventoryTransaction> findByBranchIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID branchId, LocalDateTime start, LocalDateTime end
    );

    List<InventoryTransaction> findByBranchIdAndTypeOrderByCreatedAtDesc(
            UUID branchId, InventoryTransactionType type
    );
}
