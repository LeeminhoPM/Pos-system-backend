package com.bluesky.pos_system.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockReceiptItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stock_receipt_id")
    StockReceipt stockReceipt;

    @ManyToOne(optional = false)
    Product product;

    @Column(nullable = false)
    Integer quantity;

    @Column(nullable = false)
    Double unitCost;

    @Column(nullable = false)
    Double totalPrice;
}
