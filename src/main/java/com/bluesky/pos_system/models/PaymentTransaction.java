package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.PaymentStatus;
import com.bluesky.pos_system.domains.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(nullable = false, unique = true)
    String transactionCode;

    @ManyToOne(optional = false)
    Order order;

    @Column(nullable = false)
    Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status;

    String gatewayReference;

    @Builder.Default
    String currency = "vnd";

    String chargeId;

    String receiptUrl;

    @Builder.Default
    Double refundedAmount = 0.0;

    String stripeRefundId;

    String errorMessage;

    String customerEmail;

    String cardBrand;

    String cardLast4;

    String notes;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;

    @org.hibernate.annotations.UpdateTimestamp
    LocalDateTime updatedAt;
}
