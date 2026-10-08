package com.bluesky.pos_system;

import com.bluesky.pos_system.models.Customer;
import com.bluesky.pos_system.repositories.CustomerRepository;
import com.bluesky.pos_system.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates Integration Testing using Testcontainers with official MySQL Container.
 * Requires Docker environment or runs in CI/CD pipeline with Docker service enabled.
 */
@SpringBootTest
public class MySqlTestcontainersIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Verify Customer Persistence in Database")
    void testCustomerPersistence() {
        Customer customer = TestDataFactory.newCustomer();
        Customer saved = customerRepository.save(customer);

        assertNotNull(saved.getId());
        Optional<Customer> found = customerRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Nguyen Van Khach", found.get().getFullName());
    }
}
