package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.models.Customer;
import com.bluesky.pos_system.repositories.CustomerRepository;
import com.bluesky.pos_system.services.CustomerService;
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
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(UUID id, Customer customer) {
        Customer updatedCustomer = customerRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Không tìm thấy khách hàng")
        );

        updatedCustomer.setFullName(customer.getFullName());
        updatedCustomer.setEmail(customer.getEmail());
        updatedCustomer.setPhone(customer.getPhone());
        return customerRepository.save(updatedCustomer);
    }

    @Override
    public void deleteCustomer(UUID id) {
        Customer deletedCustomer = customerRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Không tìm thấy khách hàng")
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
                () -> new RuntimeException("Không tìm thấy khách hàng")
        );
    }

    @Override
    public List<Customer> searchCustomer(String keyword) {
        return customerRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }
}
