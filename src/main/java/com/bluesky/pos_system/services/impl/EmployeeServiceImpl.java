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
import jakarta.persistence.EntityNotFoundException;
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
                () -> new EntityNotFoundException("Không tìm thấy cửa hàng")
        );

        Branch branch = null;
        if (employeeDTO.getRoles() == UserRole.ROLE_BRANCH_MANAGER || employeeDTO.getBranchId() != null) {
            if (employeeDTO.getBranchId() != null) {
                branch = branchRepository.findById(employeeDTO.getBranchId()).orElse(null);
            }
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
                () -> new EntityNotFoundException("Không tìm thấy chi nhánh")
        );
        User user = UserMapper.toEntity(employeeDTO);
        user.setBranch(branch);
        user.setStore(branch.getStore());
        user.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserDTO updateEmployee(UUID employeeId, UserDTO employeeDTO) {
        User employee = userRepository.findById(employeeId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy nhân viên")
        );

        if (employeeDTO.getBranchId() != null) {
            Branch branch = branchRepository.findById(employeeDTO.getBranchId()).orElse(null);
            if (branch != null) {
                employee.setBranch(branch);
                if (employee.getStore() == null) {
                    employee.setStore(branch.getStore());
                }
            }
        }

        if (employeeDTO.getEmail() != null) {
            employee.setEmail(employeeDTO.getEmail());
        }
        if (employeeDTO.getFullName() != null) {
            employee.setFullName(employeeDTO.getFullName());
        }
        if (employeeDTO.getPhone() != null) {
            employee.setPhone(employeeDTO.getPhone());
        }
        if (employeeDTO.getRoles() != null) {
            employee.setRoles(employeeDTO.getRoles());
        }
        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isBlank()) {
            employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        }

        return UserMapper.toDTO(userRepository.save(employee));
    }

    @Override
    public void deleteEmployee(UUID employeeId) {
        User employee = userRepository.findById(employeeId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy nhân viên")
        );
        userRepository.delete(employee);
    }

    @Override
    public List<UserDTO> findStoreEmployees(UUID storeId, UserRole role) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy cửa hàng")
        );

        List<User> employees = userRepository.findByStore(store);
        return employees.stream().filter(
                employee -> role == null || employee.getRoles() == role
        ).map(UserMapper::toDTO).toList();
    }

    @Override
    public List<UserDTO> findBranchEmployees(UUID branchId, UserRole role) {
        Branch branch = branchRepository.findById(branchId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy chi nhánh")
        );

        List<User> employees = userRepository.findByBranch(branch);
        return employees.stream().filter(
                employee -> role == null || employee.getRoles() == role
        ).map(UserMapper::toDTO).toList();
    }
}
