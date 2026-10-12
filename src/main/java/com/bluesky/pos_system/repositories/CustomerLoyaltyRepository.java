package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.CustomerLoyaltyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerLoyaltyRepository extends JpaRepository<CustomerLoyaltyTransaction, UUID> {
    List<CustomerLoyaltyTransaction> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    Page<CustomerLoyaltyTransaction> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
}
