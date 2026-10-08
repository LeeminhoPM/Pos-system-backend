package com.bluesky.pos_system.payload.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse {
    String message;
    Boolean success;
    Integer status;
    LocalDateTime timestamp;

    public ApiResponse(String message) {
        this.message = message;
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }
}
