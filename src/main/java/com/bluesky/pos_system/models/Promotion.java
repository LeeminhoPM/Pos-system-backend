package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.DiscountType;
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
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(nullable = false, unique = true)
    String code;

    @Column(nullable = false)
    String title;

    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DiscountType discountType; // PERCENTAGE or FIXED_AMOUNT

    @Column(nullable = false)
    Double discountValue;

    Double minOrderValue;

    Double maxDiscountAmount;

    LocalDateTime startDate;

    LocalDateTime endDate;

    Integer usageLimit;

    Integer usedCount;

    Boolean isActive;

    @ManyToOne
    Store store;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (usedCount == null) {
            usedCount = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (minOrderValue == null) {
            minOrderValue = 0.0;
        }
    }
}
