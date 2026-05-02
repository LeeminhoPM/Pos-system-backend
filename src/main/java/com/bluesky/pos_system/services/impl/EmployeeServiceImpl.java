package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.UserRole;
import com.bluesky.pos_system.mappers.UserMapper;
import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.UserDTO;
import com.bluesky.pos_system.repositories.BranchRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.repositories.UserRepository;
import com.bluesky.pos_system.services.EmployeeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {
    UserRepository userRepository;
    StoreRepository storeRepository;
    BranchRepository branchRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createStoreEmployee(UserDTO employeeDTO, UUID storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new RuntimeException("Không tìm thấy store")
        );

        Branch branch = null;
        if (employeeDTO.getRoles() == UserRole.ROLE_BRANCH_MANAGER) {
            if (employeeDTO.getBranchId() == null) {
                throw new RuntimeException("Branch id là cần thiết để tạo quản lý branch");
            }
            branch = branchRepository.findById(employeeDTO.getBranchId()).orElseThrow(
                    () -> new RuntimeException("Không tìm thấy branch")
            );
        }

        User user = UserMapper.toEntity(employeeDTO);
        user.setStore(store);
        user.setBranch(branch);
        user.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));

        User savedEmployee = userRepository.save(user);
        if (employeeDTO.getRoles() == UserRole.ROLE_BRANCH_MANAGER && branch != null) {
            branch.setManager(savedEmployee);
            branchRepository.save(branch);
        }

        return UserMapper.toDTO(savedEmployee);
    }

    @Override
    public UserDTO createBranchEmployee(UserDTO employeeDTO, UUID branchId) {
        Branch branch = branchRepository.findById(branchId).orElseThrow(
                () -> new RuntimeException("Không tìm thấy branch")
        );
        if (employeeDTO.getRoles() == UserRole.ROLE_BRANCH_CASHIER || employeeDTO.getRoles() == UserRole.ROLE_BRANCH_MANAGER) {
            User user = UserMapper.toEntity(employeeDTO);
            user.setBranch(branch);
            user.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
            return UserMapper.toDTO(userRepository.save(user));
        }
        throw new RuntimeException("Không có quyền branch");
    }

    @Override
    public UserDTO updateEmployee(UUID employeeId, UserDTO employeeDTO) {
        User employee = userRepository.findById(employeeId).orElseThrow(
                () -> new RuntimeException("Không tìm thấy người dùng")
        );

        Branch branch = branchRepository.findById(employeeDTO.getBranchId()).orElseThrow(
                () -> new RuntimeException("Không tìm thấy branch")
        );

        employee.setEmail(employeeDTO.getEmail());
        employee.setFullName(employeeDTO.getFullName());
        employee.setPhone(employeeDTO.getPhone());
        employee.setRoles(employeeDTO.getRoles());
        employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        employee.setBranch(branch);

        return UserMapper.toDTO(userRepository.save(employee));
    }

    @Override
    public void deleteEmployee(UUID employeeId) {
        User employee = userRepository.findById(employeeId).orElseThrow(
                () -> new RuntimeException("Không tìm thấy người dùng")
        );
        userRepository.delete(employee);
    }

    @Override
    public List<UserDTO> findStoreEmployees(UUID storeId, UserRole role) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new RuntimeException("Không tìm thấy store")
        );

        List<User> employees = userRepository.findByStore(store);
        return employees.stream().filter(
                employee -> role == null || employee.getRoles() == role
        ).map(UserMapper::toDTO).toList();
    }

    @Override
    public List<UserDTO> findBranchEmployees(UUID branchId, UserRole role) {
        Branch branch = branchRepository.findById(branchId).orElseThrow(
                () -> new RuntimeException("Không tìm thấy branch")
        );

        List<User> employees = userRepository.findByBranch(branch);
        return employees.stream().filter(
                employee -> role == null || employee.getRoles() == role
        ).map(UserMapper::toDTO).toList();
    }
}
