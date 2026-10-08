package com.bluesky.pos_system.services;

import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.SupplierDTO;

import java.util.List;
import java.util.UUID;

public interface SupplierService {
    SupplierDTO createSupplier(SupplierDTO dto, User user);

    SupplierDTO updateSupplier(UUID id, SupplierDTO dto);

    void deleteSupplier(UUID id);

    SupplierDTO getSupplierById(UUID id);

    List<SupplierDTO> getSuppliersByStore(UUID storeId);

    List<SupplierDTO> searchSuppliers(UUID storeId, String query);
}
