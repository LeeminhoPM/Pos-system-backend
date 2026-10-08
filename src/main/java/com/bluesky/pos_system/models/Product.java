package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, unique = true)
    String sku;

    String barcode;

    String description;

    Double mrp;

    Double costPrice;

    Double sellingPrice;

    Double vatRate; // e.g. 0.08 for 8% VAT

    String brand;

    String image;

    Integer minStockLevel;

    Boolean isActive;

    @Enumerated(EnumType.STRING)
    ProductStatus status;

    Boolean isDeleted;

    LocalDateTime deletedAt;

    @ManyToOne
    Category category;

    @ManyToOne
    Supplier supplier;

    @ManyToOne
    Store store;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (status == null) {
            status = ProductStatus.IN_STOCK;
        }
        if (minStockLevel == null) {
            minStockLevel = 5;
        }
        if (vatRate == null) {
            vatRate = 0.08;
        }
        if (barcode == null || barcode.isBlank()) {
            barcode = sku;
        }
    }

    @Transient
    public Double getProfitAmount() {
        if (sellingPrice == null || costPrice == null) return 0.0;
        return sellingPrice - costPrice;
    }

    @Transient
    public Double getProfitMargin() {
        if (sellingPrice == null || sellingPrice == 0 || costPrice == null) return 0.0;
        return ((sellingPrice - costPrice) / sellingPrice) * 100.0;
    }
}
