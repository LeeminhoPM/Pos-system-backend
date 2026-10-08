package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.exceptions.UserException;
import com.bluesky.pos_system.mappers.UserMapper;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.ChangePasswordDTO;
import com.bluesky.pos_system.payload.dto.UserDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/user", "/api/user"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User & Profile Management", description = "APIs for user profile, authentication context, and password reset")
public class UserController {
    UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get user profile using JWT token")
    public ResponseEntity<UserDTO> getUserProfile(@RequestHeader(value = "Authorization", required = false) String jwtToken) throws UserException {
        User user = (jwtToken != null) ? userService.getUserFromJwtToken(jwtToken) : userService.getCurrentUser();
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info")
    public ResponseEntity<UserDTO> getCurrentUser() throws UserException {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) throws UserException {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for the current user")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordDTO dto) throws UserException {
        userService.changePassword(dto);
        ApiResponse res = new ApiResponse();
        res.setMessage("Đổi mật khẩu thành công");
        return ResponseEntity.ok(res);
    }
}
