package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.UserDTO;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMapper {
    public static UserDTO toDTO(User savedUser) {
        return UserDTO.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .roles(savedUser.getRoles())
                .lastLogin(savedUser.getLastLogin())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .branchId(savedUser.getBranch() != null ? savedUser.getBranch().getId() : null)
                .storeId(savedUser.getStore() != null ? savedUser.getStore().getId() : null)
                .build();
    }

    public static User toEntity(UserDTO userDTO) {
        return User.builder()
                .email(userDTO.getEmail())
                .fullName(userDTO.getFullName())
                .phone(userDTO.getPhone())
                .roles(userDTO.getRoles())
                .lastLogin(userDTO.getLastLogin())
                .build();
    }
}
