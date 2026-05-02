package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.PaymentType;
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
@Table(name = "order_tbl")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    Double totalAmount;

    PaymentType paymentType;

    @ManyToOne
    Branch branch;

    @ManyToOne
    User cashier;

    @ManyToOne
    Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    List<OrderItem> items;

    @Column(updatable = false)
    @CreationTimestamp
    LocalDateTime createdAt;
}
