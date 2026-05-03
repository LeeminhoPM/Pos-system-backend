package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.PaymentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne
    Order order;

    String reason;

    Double amount;

    @ManyToOne
    @JsonIgnore
    ShiftReport shiftReport;

    @ManyToOne
    User cashier;

    @ManyToOne
    Branch branch;

    PaymentType paymentType;

    @Column(updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;
}
