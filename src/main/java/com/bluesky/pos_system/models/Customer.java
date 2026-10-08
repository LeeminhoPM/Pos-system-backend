package com.bluesky.pos_system.models;

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
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(unique = true)
    String customerCode;

    @Column(nullable = false)
    String fullName;

    String email;

    String phone;

    String address;

    Integer loyaltyPoints;

    Double totalSpent;

    Boolean isDeleted;

    @Column(updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (loyaltyPoints == null) {
            loyaltyPoints = 0;
        }
        if (totalSpent == null) {
            totalSpent = 0.0;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (customerCode == null || customerCode.isBlank()) {
            customerCode = "CUST-" + System.currentTimeMillis() % 1000000;
        }
    }
}
