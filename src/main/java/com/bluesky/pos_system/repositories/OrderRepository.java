package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerId(UUID customerId);

    List<Order> findByBranchId(UUID branchId);

    List<Order> findByCashierId(UUID cashierId);

    List<Order> findByBranchIdAndCreatedAtBetween(UUID branch_id, LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByCashierIdAndCreatedAtBetween(UUID cashierId, LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findTop5ByBranchIdOrderByCreatedAtDesc(UUID branchId);
}
