package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    @EntityGraph(attributePaths = {"branch", "cashier", "customer", "items", "items.product"})
    Optional<Order> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"branch", "cashier", "customer"})
    List<Order> findByCustomerId(UUID customerId);

    @EntityGraph(attributePaths = {"branch", "cashier", "customer"})
    List<Order> findByBranchId(UUID branchId);

    @EntityGraph(attributePaths = {"branch", "cashier", "customer"})
    List<Order> findByCashierId(UUID cashierId);

    @EntityGraph(attributePaths = {"branch", "cashier", "customer"})
    List<Order> findByBranchIdAndCreatedAtBetween(UUID branch_id, LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = {"branch", "cashier", "customer"})
    List<Order> findByCashierIdAndCreatedAtBetween(UUID cashierId, LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = {"branch", "cashier", "customer"})
    List<Order> findTop5ByBranchIdOrderByCreatedAtDesc(UUID branchId);

    @EntityGraph(attributePaths = {"branch", "cashier", "customer"})
    @Query("SELECT o FROM Order o WHERE o.branch.id = :branchId " +
           "AND (:customerId IS NULL OR o.customer.id = :customerId) " +
           "AND (:cashierId IS NULL OR o.cashier.id = :cashierId) " +
           "AND (:paymentType IS NULL OR o.paymentType = :paymentType) " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<Order> findFilteredOrders(
            @Param("branchId") UUID branchId,
            @Param("customerId") UUID customerId,
            @Param("cashierId") UUID cashierId,
            @Param("paymentType") PaymentType paymentType,
            @Param("status") OrderStatus status,
            Pageable pageable
    );
}
