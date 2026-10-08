package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.models.Customer;
import com.bluesky.pos_system.repositories.CustomerRepository;
import com.bluesky.pos_system.services.CustomerService;
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
public class CustomerServiceImpl implements CustomerService {
    CustomerRepository customerRepository;

    @Override
    public Customer createCustomer(Customer customer) {
        if (customer.getLoyaltyPoints() == null) {
            customer.setLoyaltyPoints(0);
        }
        if (customer.getTotalSpent() == null) {
            customer.setTotalSpent(0.0);
        }
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(UUID id, Customer customer) {
        Customer updatedCustomer = customerRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy khách hàng")
        );

        updatedCustomer.setFullName(customer.getFullName());
        updatedCustomer.setEmail(customer.getEmail());
        updatedCustomer.setPhone(customer.getPhone());
        if (customer.getAddress() != null) {
            updatedCustomer.setAddress(customer.getAddress());
        }
        if (customer.getLoyaltyPoints() != null) {
            updatedCustomer.setLoyaltyPoints(customer.getLoyaltyPoints());
        }
        if (customer.getTotalSpent() != null) {
            updatedCustomer.setTotalSpent(customer.getTotalSpent());
        }
        return customerRepository.save(updatedCustomer);
    }

    @Override
    public void deleteCustomer(UUID id) {
        Customer deletedCustomer = customerRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy khách hàng")
        );
        customerRepository.delete(deletedCustomer);
    }

    @Override
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer findCustomerById(UUID id) {
        return customerRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy khách hàng")
        );
    }

    @Override
    public List<Customer> searchCustomer(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return customerRepository.findAll();
        }
        return customerRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(keyword, keyword, keyword);
    }

    @Override
    public com.bluesky.pos_system.payload.dto.PageResponse<Customer> getCustomersPaged(int page, int size, String keyword) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<Customer> pageResult;
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            pageResult = customerRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(kw, kw, kw, pageRequest);
        } else {
            pageResult = customerRepository.findAll(pageRequest);
        }

        return com.bluesky.pos_system.payload.dto.PageResponse.<Customer>builder()
                .content(pageResult.getContent())
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .build();
    }
}
