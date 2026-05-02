package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.UserRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDTO {
    UUID id;

    String fullName;

    String email;

    String phone;

    UserRole roles;

    String password;

    UUID branchId;

    UUID storeId;

    LocalDate createdAt;

    LocalDate updatedAt;

    LocalDateTime lastLogin;
}
