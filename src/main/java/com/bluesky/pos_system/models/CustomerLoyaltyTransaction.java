package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.LoyaltyTransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_loyalty_transaction", indexes = {
        @Index(name = "idx_loyalty_txn_customer", columnList = "customer_id"),
        @Index(name = "idx_loyalty_txn_created", columnList = "created_at")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerLoyaltyTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    LoyaltyTransactionType type;

    @Column(nullable = false)
    Integer pointsChange;

    @Column(nullable = false)
    Integer balanceAfter;

    String description;

    @Column(updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;
}
