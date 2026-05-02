package com.bluesky.pos_system.services;

import com.bluesky.pos_system.models.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    Customer createCustomer(Customer customer);

    Customer updateCustomer(UUID id, Customer customer);

    void deleteCustomer(UUID id);

    List<Customer> findAllCustomers();

    Customer findCustomerById(UUID id);

    List<Customer> searchCustomer(String keyword);
}
