package com.bluesky.pos_system.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
public class ShiftReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    LocalDateTime shiftStart;

    LocalDateTime shiftEnd;

    Double totalSales;

    Double totalRefunds;

    Double netSale;

    int totalOrders;

    @ManyToOne
    User cashier;

    @ManyToOne
    Branch branch;

    @Transient
    List<PaymentSummary> paymentSummaries;

    @OneToMany
    List<Product> topSellingProducts;

    @OneToMany(cascade = CascadeType.ALL)
    List<Order> recentOrders;

    @OneToMany(mappedBy = "shiftReport", cascade = CascadeType.ALL)
    List<Refund> refunds;
}