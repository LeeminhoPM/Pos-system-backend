package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.StoreStatus;
import com.bluesky.pos_system.models.StoreContact;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreDTO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    UUID id;

    String branch;

    UserDTO storeAdmin;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    String description;

    String storeType;

    StoreStatus status;

    StoreContact contact;
}
