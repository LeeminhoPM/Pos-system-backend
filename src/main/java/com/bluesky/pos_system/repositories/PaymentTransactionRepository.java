package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.domains.PaymentStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.models.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    List<PaymentTransaction> findByOrderId(UUID orderId);

    Optional<PaymentTransaction> findByTransactionCode(String transactionCode);

    Optional<PaymentTransaction> findByGatewayReference(String gatewayReference);

    List<PaymentTransaction> findByStatus(PaymentStatus status);

    List<PaymentTransaction> findByPaymentType(PaymentType paymentType);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.order.branch.id = :branchId " +
            "AND pt.createdAt BETWEEN :start AND :end")
    List<PaymentTransaction> findByBranchAndDateRange(
            @Param("branchId") UUID branchId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
