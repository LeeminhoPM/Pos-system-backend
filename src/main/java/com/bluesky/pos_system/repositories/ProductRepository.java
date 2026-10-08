package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.domains.ProductStatus;
import com.bluesky.pos_system.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByStoreId(UUID storeId);

    List<Product> findByStoreIdAndIsDeletedFalse(UUID storeId);

    Page<Product> findByStoreIdAndIsDeletedFalse(UUID storeId, Pageable pageable);

    Optional<Product> findByIdAndIsDeletedFalse(UUID id);

    Optional<Product> findBySkuAndStoreIdAndIsDeletedFalse(String sku, UUID storeId);

    Optional<Product> findByBarcodeAndStoreIdAndIsDeletedFalse(String barcode, UUID storeId);

    List<Product> findByStoreIdAndCategoryIdAndIsDeletedFalse(UUID storeId, UUID categoryId);

    List<Product> findByStoreIdAndSupplierIdAndIsDeletedFalse(UUID storeId, UUID supplierId);

    List<Product> findByStoreIdAndStatusAndIsDeletedFalse(UUID storeId, ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.isDeleted = false AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")")
    List<Product> searchByKeyword(@Param("storeId") UUID storeId, @Param("query") String query);

    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.isDeleted = false AND (" +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:status IS NULL OR p.status = :status)" +
            ")")
    Page<Product> filterProducts(
            @Param("storeId") UUID storeId,
            @Param("keyword") String keyword,
            @Param("categoryId") UUID categoryId,
            @Param("status") ProductStatus status,
            Pageable pageable
    );
}
