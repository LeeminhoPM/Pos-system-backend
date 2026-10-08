package com.bluesky.pos_system.payload.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordDTO {
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    String newPassword;
}
