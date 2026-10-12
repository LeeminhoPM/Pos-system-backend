package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.domains.DeliveryStatus;
import com.bluesky.pos_system.models.DeliveryOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, UUID> {
    Optional<DeliveryOrder> findByOrderId(UUID orderId);

    Optional<DeliveryOrder> findByTrackingNumber(String trackingNumber);

    Page<DeliveryOrder> findByOrder_Branch_IdAndDeliveryStatus(UUID branchId, DeliveryStatus status, Pageable pageable);

    Page<DeliveryOrder> findByOrder_Branch_Id(UUID branchId, Pageable pageable);
}
