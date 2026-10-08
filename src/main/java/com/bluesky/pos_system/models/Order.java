package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    String orderNumber;

    Double subtotal;

    Double discount;

    Double tax;

    Double totalAmount;

    @Enumerated(EnumType.STRING)
    OrderStatus status;

    @Enumerated(EnumType.STRING)
    PaymentType paymentType;

    String notes;

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

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = OrderStatus.COMPLETED;
        }
        if (discount == null) {
            discount = 0.0;
        }
        if (tax == null) {
            tax = 0.0;
        }
    }
}
