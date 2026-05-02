package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(nullable = false)
    String fullName;

    @Column(nullable = false, unique = true)
    @Email(message = "Email không hợp lệ")
    String email;

    String phone;

    @ManyToOne
    Store store;

    @ManyToOne
    Branch branch;

    @Column(nullable = false)
    UserRole roles;

    @Column(nullable = false)
    String password;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDate createdAt;

    @UpdateTimestamp
    LocalDate updatedAt;

    LocalDateTime lastLogin;
}
