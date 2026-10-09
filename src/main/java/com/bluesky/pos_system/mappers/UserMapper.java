package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.UserDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;

public class UserMapper {
    public static UserDTO toDTO(User savedUser) {
        if (savedUser == null) return null;
        UUID bId = null;
        try {
            if (savedUser.getBranch() != null) {
                bId = savedUser.getBranch().getId();
            }
        } catch (Exception ignored) {
        }

        UUID sId = null;
        try {
            if (savedUser.getStore() != null) {
                sId = savedUser.getStore().getId();
            }
        } catch (Exception ignored) {
        }

        return UserDTO.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .roles(savedUser.getRoles())
                .lastLogin(savedUser.getLastLogin())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .branchId(bId)
                .storeId(sId)
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
