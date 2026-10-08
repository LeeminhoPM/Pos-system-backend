package com.bluesky.pos_system.repositories;

import com.bluesky.pos_system.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(String fullName, String email, String phone);

    Page<Customer> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(String fullName, String email, String phone, Pageable pageable);
}
