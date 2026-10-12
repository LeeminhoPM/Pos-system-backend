package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_order", indexes = {
        @Index(name = "idx_delivery_order_id", columnList = "order_id"),
        @Index(name = "idx_delivery_status", columnList = "delivery_status"),
        @Index(name = "idx_delivery_tracking", columnList = "tracking_number")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    Order order;

    @Column(nullable = false)
    String recipientName;

    @Column(nullable = false)
    String recipientPhone;

    @Column(nullable = false, length = 500)
    String shippingAddress;

    @Builder.Default
    Double shippingFee = 0.0;

    String trackingNumber;

    String deliveryCarrier; // ví dụ: GrabExpress, GHTK, GHN, AhaMove, Nội bộ

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    DeliveryStatus deliveryStatus;

    LocalDateTime estimatedDeliveryTime;

    LocalDateTime deliveredAt;

    String notes;

    @Column(updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (deliveryStatus == null) {
            deliveryStatus = DeliveryStatus.PENDING;
        }
        if (shippingFee == null) {
            shippingFee = 0.0;
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            trackingNumber = "DELIV-" + System.currentTimeMillis() % 1000000;
        }
    }
}
