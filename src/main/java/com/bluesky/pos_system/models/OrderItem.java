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
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    Integer quantity;

    Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    Order order;
}
