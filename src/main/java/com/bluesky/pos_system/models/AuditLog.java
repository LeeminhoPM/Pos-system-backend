package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.AuditAction;
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
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AuditAction action;

    @Column(nullable = false)
    String entityName;

    String entityId;

    String performedBy;

    @Column(length = 2000)
    String details;

    String ipAddress;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime timestamp;
}
