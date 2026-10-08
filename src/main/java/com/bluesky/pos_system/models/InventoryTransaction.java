package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.InventoryTransactionType;
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
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne(optional = false)
    Branch branch;

    @ManyToOne(optional = false)
    Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    InventoryTransactionType type;

    @Column(nullable = false)
    Integer quantityChange;

    @Column(nullable = false)
    Integer balanceAfter;

    String referenceNumber; // e.g. Order number or Receipt number

    String notes;

    @ManyToOne
    User createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;
}
