package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByStoreId(UUID storeId);

    List<Category> findByStoreIdAndParentIsNull(UUID storeId);

    Optional<Category> findBySlugAndStoreId(String slug, UUID storeId);
}
