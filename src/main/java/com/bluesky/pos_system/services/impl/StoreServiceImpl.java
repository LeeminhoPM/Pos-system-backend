package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.StoreStatus;
import com.bluesky.pos_system.exceptions.UserException;
import com.bluesky.pos_system.mappers.StoreMapper;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.StoreContact;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.StoreDTO;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.services.StoreService;
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
public class StoreServiceImpl implements StoreService {
    StoreRepository storeRepository;
    UserService userService;

    @Override
    @Transactional
    public StoreDTO createStore(StoreDTO storeDTO, User user) {
        Store store = StoreMapper.toEntity(storeDTO, user);
        return StoreMapper.toDTO(storeRepository.save(store));
    }

    @Override
    public StoreDTO getStoreById(UUID storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new RuntimeException("Cửa hàng với id: " + storeId + " không tìm thấy")
        );
        return StoreMapper.toDTO(store);
    }

    @Override
    public List<StoreDTO> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream().map(StoreMapper::toDTO).toList();
    }

    @Override
    public Store getStoreByAdmin() {
        User admin = userService.getCurrentUser();
        return storeRepository.findByStoreAdminId(admin.getId());
    }

    @Override
    @Transactional
    public StoreDTO updateStore(StoreDTO storeDTO, UUID storeId) {
        User currentUser = userService.getCurrentUser();
        Store existingStore = storeRepository.findByStoreAdminId(currentUser.getId());
        if (existingStore == null) {
            throw new RuntimeException("Không tìm thấy của hàng");
        }

        existingStore.setBranch(storeDTO.getBranch());
        existingStore.setDescription(storeDTO.getDescription());
        if (storeDTO.getStoreType() != null) {
            existingStore.setStoreType(storeDTO.getStoreType());
        }
        if (storeDTO.getContact() != null) {
            StoreContact contact = StoreContact.builder()
                    .address(storeDTO.getContact().getAddress())
                    .phone(storeDTO.getContact().getPhone())
                    .email(storeDTO.getContact().getEmail())
                    .build();
            existingStore.setContact(contact);
        }

        return StoreMapper.toDTO(storeRepository.save(existingStore));
    }

    @Override
    @Transactional
    public void deleteStore(UUID storeId) {
        Store store = getStoreByAdmin();
        storeRepository.delete(store);
    }

    @Override
    public StoreDTO getStoreByEmployee() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new UserException("Bạn không có quyền truy cập vào cửa hàng này");
        }
        return StoreMapper.toDTO(currentUser.getStore());
    }

    @Override
    @Transactional
    public StoreDTO moderateStore(UUID storeId, StoreStatus storeStatus) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng"));
        store.setStatus(storeStatus);
        return StoreMapper.toDTO(storeRepository.save(store));
    }
}
