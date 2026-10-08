package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    List<Supplier> findByStoreId(UUID storeId);

    Optional<Supplier> findByCode(String code);

    @Query("SELECT s FROM Supplier s WHERE s.store.id = :storeId AND (" +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")")
    List<Supplier> searchSuppliers(@Param("storeId") UUID storeId, @Param("query") String query);
}
