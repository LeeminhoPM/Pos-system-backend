package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    List<Promotion> findByStoreIdOrderByCreatedAtDesc(UUID storeId);

    Optional<Promotion> findByCodeIgnoreCase(String code);

    @Query("SELECT p FROM Promotion p WHERE p.code = :code AND p.isActive = true " +
            "AND (p.startDate IS NULL OR p.startDate <= :now) " +
            "AND (p.endDate IS NULL OR p.endDate >= :now) " +
            "AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)")
    Optional<Promotion> findValidPromotionByCode(@Param("code") String code, @Param("now") LocalDateTime now);

    @Query("SELECT p FROM Promotion p WHERE p.store.id = :storeId AND p.isActive = true " +
            "AND (p.startDate IS NULL OR p.startDate <= :now) " +
            "AND (p.endDate IS NULL OR p.endDate >= :now)")
    List<Promotion> findActivePromotions(@Param("storeId") UUID storeId, @Param("now") LocalDateTime now);
}
