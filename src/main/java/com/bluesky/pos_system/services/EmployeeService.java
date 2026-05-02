package com.bluesky.pos_system.services;

import com.bluesky.pos_system.domains.UserRole;
import com.bluesky.pos_system.payload.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    UserDTO createStoreEmployee(UserDTO employeeDTO, UUID storeId);

    UserDTO createBranchEmployee(UserDTO employeeDTO, UUID branchId);

    UserDTO updateEmployee(UUID employeeId, UserDTO employeeDTO);

    void deleteEmployee(UUID employeeId);

    List<UserDTO> findStoreEmployees(UUID storeId, UserRole role);

    List<UserDTO> findBranchEmployees(UUID branchId, UserRole role);
}
