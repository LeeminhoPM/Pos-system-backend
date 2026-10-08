package com.bluesky.pos_system.payload.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Standard RESTful API Envelope")
public class ApiResponse<T> {

    @Schema(description = "Trạng thái thành công hay thất bại", example = "true")
    Boolean success;

    @Schema(description = "Thông điệp phản hồi từ hệ thống", example = "Thao tác thành công")
    String message;

    @Schema(description = "Dữ liệu trả về")
    T data;

    @Schema(description = "Mã trạng thái HTTP", example = "200")
    Integer status;

    @Schema(description = "Thời gian phản hồi")
    LocalDateTime timestamp;

    public ApiResponse(String message) {
        this.message = message;
        this.success = true;
        this.status = 200;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Integer status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
