package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Inventory findByProductIdAndBranchId(UUID productId, UUID branchId);

    List<Inventory> findByBranchId(UUID branchId);
}
