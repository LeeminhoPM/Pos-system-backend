package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.mappers.BranchMapper;
import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.BranchDTO;
import com.bluesky.pos_system.repositories.BranchRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.services.BranchService;
import com.bluesky.pos_system.services.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchServiceImpl implements BranchService {
    BranchRepository branchRepository;
    StoreRepository storeRepository;
    UserService userService;

    @Override
    @Transactional
    public BranchDTO createBranch(BranchDTO branchDTO) {
        User user = userService.getCurrentUser();
        Store store = storeRepository.findByStoreAdminId(user.getId());
        Branch branch = BranchMapper.toEntity(branchDTO, store);
        return BranchMapper.toDTO(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchDTO updateBranch(UUID id, BranchDTO branchDTO) {
        Branch branch = branchRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));

        branch.setName(branchDTO.getName());
        branch.setWorkingDays(branchDTO.getWorkingDays());
        branch.setEmail(branchDTO.getEmail());
        branch.setPhone(branchDTO.getPhone());
        branch.setAddress(branchDTO.getAddress());
        branch.setOpenTime(branchDTO.getOpenTime());
        branch.setCloseTime(branchDTO.getCloseTime());

        return BranchMapper.toDTO(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void deleteBranch(UUID id) {
        Branch branch = branchRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        branchRepository.delete(branch);
    }

    @Override
    public List<BranchDTO> getAllBranchesByStoreId(UUID storeId) {
        List<Branch> branches = branchRepository.findByStoreId(storeId);
        return branches.stream().map(BranchMapper::toDTO).toList();
    }

    @Override
    public BranchDTO getBranchById(UUID id) {
        Branch branch = branchRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        return BranchMapper.toDTO(branch);
    }
}
