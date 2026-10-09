package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    @EntityGraph(attributePaths = {"product", "product.category", "product.supplier", "branch"})
    Inventory findByProductIdAndBranchId(UUID productId, UUID branchId);

    @EntityGraph(attributePaths = {"product", "product.category", "product.supplier", "branch"})
    List<Inventory> findByBranchId(UUID branchId);

    @EntityGraph(attributePaths = {"product", "product.category", "product.supplier", "branch"})
    List<Inventory> findByProductId(UUID productId);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalStockByProductId(@Param("productId") UUID productId);

    @EntityGraph(attributePaths = {"product", "product.category", "product.supplier", "branch"})
    @Query("SELECT i FROM Inventory i WHERE i.branch.id = :branchId AND i.quantity <= COALESCE(i.product.minStockLevel, 5)")
    List<Inventory> findLowStockByBranchId(@Param("branchId") UUID branchId);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.branch.id = :branchId AND i.quantity <= COALESCE(i.product.minStockLevel, 5)")
    Long countLowStockByBranchId(@Param("branchId") UUID branchId);
}
