package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.mappers.SupplierMapper;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.Supplier;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.SupplierDTO;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.repositories.SupplierRepository;
import com.bluesky.pos_system.services.SupplierService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SupplierServiceImpl implements SupplierService {
    SupplierRepository supplierRepository;
    StoreRepository storeRepository;

    @Override
    public SupplierDTO createSupplier(SupplierDTO dto, User user) {
        UUID storeId = dto.getStoreId();
        if (storeId == null && user != null && user.getStore() != null) {
            storeId = user.getStore().getId();
        }
        if (storeId == null) {
            throw new RuntimeException("Cửa hàng không được để trống");
        }

        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy cửa hàng với ID: " + dto.getStoreId())
        );

        Supplier supplier = SupplierMapper.toEntity(dto, store);
        return SupplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    public SupplierDTO updateSupplier(UUID id, SupplierDTO dto) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy nhà cung cấp")
        );

        supplier.setName(dto.getName());
        supplier.setContactName(dto.getContactName());
        supplier.setPhone(dto.getPhone());
        supplier.setEmail(dto.getEmail());
        supplier.setAddress(dto.getAddress());
        supplier.setTaxCode(dto.getTaxCode());
        supplier.setNotes(dto.getNotes());
        if (dto.getIsActive() != null) {
            supplier.setIsActive(dto.getIsActive());
        }

        return SupplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    public void deleteSupplier(UUID id) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy nhà cung cấp")
        );
        supplierRepository.delete(supplier);
    }

    @Override
    public SupplierDTO getSupplierById(UUID id) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy nhà cung cấp")
        );
        return SupplierMapper.toDTO(supplier);
    }

    @Override
    public List<SupplierDTO> getSuppliersByStore(UUID storeId) {
        return supplierRepository.findByStoreId(storeId).stream()
                .map(SupplierMapper::toDTO)
                .toList();
    }

    @Override
    public List<SupplierDTO> searchSuppliers(UUID storeId, String query) {
        if (query == null || query.isBlank()) {
            return getSuppliersByStore(storeId);
        }
        return supplierRepository.searchSuppliers(storeId, query).stream()
                .map(SupplierMapper::toDTO)
                .toList();
    }
}
