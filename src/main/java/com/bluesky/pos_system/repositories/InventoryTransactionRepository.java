package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.domains.InventoryTransactionType;
import com.bluesky.pos_system.models.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    @EntityGraph(attributePaths = {"branch", "product", "createdBy"})
    List<InventoryTransaction> findByBranchIdOrderByCreatedAtDesc(UUID branchId);

    @EntityGraph(attributePaths = {"branch", "product", "createdBy"})
    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(UUID productId);

    @EntityGraph(attributePaths = {"branch", "product", "createdBy"})
    Page<InventoryTransaction> findByBranchIdOrderByCreatedAtDesc(UUID branchId, Pageable pageable);

    @EntityGraph(attributePaths = {"branch", "product", "createdBy"})
    List<InventoryTransaction> findByBranchIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID branchId, LocalDateTime start, LocalDateTime end
    );

    @EntityGraph(attributePaths = {"branch", "product", "createdBy"})
    List<InventoryTransaction> findByBranchIdAndTypeOrderByCreatedAtDesc(
            UUID branchId, InventoryTransactionType type
    );
}
