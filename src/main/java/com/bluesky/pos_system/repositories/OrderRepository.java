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

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE (:branchId IS NULL OR o.branch.id = :branchId) AND o.status = :status")
    Double sumTotalRevenueByBranchAndStatus(@Param("branchId") UUID branchId, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o.id) FROM Order o WHERE (:branchId IS NULL OR o.branch.id = :branchId) AND o.status = :status")
    Long countOrdersByBranchAndStatus(@Param("branchId") UUID branchId, @Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE (:branchId IS NULL OR o.branch.id = :branchId) AND o.status = :status AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Double sumRevenueByDateRange(@Param("branchId") UUID branchId, @Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o.id) FROM Order o WHERE (:branchId IS NULL OR o.branch.id = :branchId) AND o.status = :status AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long countOrdersByDateRange(@Param("branchId") UUID branchId, @Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT CAST(FUNCTION('DATE', o.createdAt) as string) as saleDate, " +
           "COALESCE(SUM(o.totalAmount), 0.0) as totalRevenue, " +
           "COUNT(o.id) as orderCount " +
           "FROM Order o " +
           "WHERE (:branchId IS NULL OR o.branch.id = :branchId) " +
           "AND o.status = :status " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY FUNCTION('DATE', o.createdAt) " +
           "ORDER BY FUNCTION('DATE', o.createdAt) ASC")
    List<com.bluesky.pos_system.payload.dto.projection.DailySalesProjection> findDailySalesAggregated(
            @Param("branchId") UUID branchId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT i.product.id as productId, " +
           "i.product.name as productName, " +
           "i.product.sku as sku, " +
           "SUM(i.quantity) as totalQuantitySold, " +
           "COALESCE(SUM(i.price), 0.0) as totalRevenue " +
           "FROM OrderItem i JOIN i.order o " +
           "WHERE (:branchId IS NULL OR o.branch.id = :branchId) " +
           "AND o.status = :status " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY i.product.id, i.product.name, i.product.sku " +
           "ORDER BY SUM(i.quantity) DESC")
    List<com.bluesky.pos_system.payload.dto.projection.TopProductProjection> findTopProductsAggregated(
            @Param("branchId") UUID branchId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT o.paymentType as paymentType, " +
           "COALESCE(SUM(o.totalAmount), 0.0) as totalRevenue, " +
           "COUNT(o.id) as orderCount " +
           "FROM Order o " +
           "WHERE (:branchId IS NULL OR o.branch.id = :branchId) " +
           "AND o.status = :status " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY o.paymentType")
    List<com.bluesky.pos_system.payload.dto.projection.PaymentTypeRevenueProjection> findPaymentTypeRevenueAggregated(
            @Param("branchId") UUID branchId,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
