package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.StockReceiptType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(nullable = false, unique = true)
    String receiptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StockReceiptType type;

    @ManyToOne(optional = false)
    Branch branch;

    @ManyToOne
    Supplier supplier;

    @ManyToOne
    User createdBy;

    Double totalAmount;

    String notes;

    @OneToMany(mappedBy = "stockReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    List<StockReceiptItem> items;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;
}
