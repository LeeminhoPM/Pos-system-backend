package com.bluesky.pos_system.payload.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerRequestDTO {
    @NotBlank(message = "Họ và tên khách hàng không được để trống")
    String fullName;

    @Email(message = "Email không đúng định dạng")
    String email;

    @Pattern(regexp = "^(0|\\+84)?[0-9]{9,11}$", message = "Số điện thoại không hợp lệ")
    String phone;

    String address;

    Integer loyaltyPoints;

    Double totalSpent;
}
