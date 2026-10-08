package com.bluesky.pos_system.payload.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupplierDTO {
    UUID id;

    String code;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    String name;

    String contactName;

    String phone;

    String email;

    String address;

    String taxCode;

    String notes;

    UUID storeId;

    Boolean isActive;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
