package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.domains.UserRole;
import com.bluesky.pos_system.payload.dto.UserDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.EmployeeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeController {
    EmployeeService employeeService;

    @PostMapping("/store/{storeId}")
    public ResponseEntity<UserDTO> createStoreEmployee(@PathVariable UUID storeId, @RequestBody UserDTO userDTO) {
        UserDTO response = employeeService.createStoreEmployee(userDTO, storeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/branch/{branchId}")
    public ResponseEntity<UserDTO> createBranchEmployee(@PathVariable UUID branchId, @RequestBody UserDTO userDTO) {
        UserDTO response = employeeService.createBranchEmployee(userDTO, branchId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateEmployee(@PathVariable UUID id, @RequestBody UserDTO userDTO) {
        UserDTO response = employeeService.updateEmployee(id, userDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteEmployee(@PathVariable UUID id) {
        employeeService.deleteEmployee(id);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Xóa nhân viên thành công");
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<UserDTO>> getStoreEmployees(@PathVariable UUID storeId, @RequestParam(required = false) UserRole userRole) {
        List<UserDTO> response = employeeService.findStoreEmployees(storeId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<UserDTO>> getBranchEmployees(@PathVariable UUID branchId, @RequestParam(required = false) UserRole userRole) {
        List<UserDTO> response = employeeService.findBranchEmployees(branchId, userRole);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
