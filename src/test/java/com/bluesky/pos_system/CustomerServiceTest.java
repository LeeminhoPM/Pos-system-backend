package com.bluesky.pos_system;

import com.bluesky.pos_system.models.Customer;
import com.bluesky.pos_system.repositories.CustomerRepository;
import com.bluesky.pos_system.services.impl.CustomerServiceImpl;
import com.bluesky.pos_system.util.TestDataFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = TestDataFactory.createCustomer();
    }

    @Test
    @DisplayName("Create Customer - Success with Default Points and TotalSpent")
    void testCreateCustomer_Success() {
        Customer newCustomer = Customer.builder()
                .fullName("Le Van C")
                .email("levanc@test.com")
                .phone("0912345678")
                .build();

        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        Customer result = customerService.createCustomer(newCustomer);

        assertNotNull(result);
        assertEquals("Le Van C", result.getFullName());
        assertEquals(0, result.getLoyaltyPoints());
        assertEquals(0.0, result.getTotalSpent());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Update Customer - Success")
    void testUpdateCustomer_Success() {
        UUID id = customer.getId();
        Customer updateReq = Customer.builder()
                .fullName("Nguyen Van Khach Updated")
                .email("updated@test.com")
                .phone("0999999999")
                .address("New Address")
                .build();

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.updateCustomer(id, updateReq);

        assertNotNull(result);
        assertEquals("Nguyen Van Khach Updated", result.getFullName());
        assertEquals("updated@test.com", result.getEmail());
        assertEquals("New Address", result.getAddress());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("Find Customer By ID - Throws Exception When Not Found")
    void testFindCustomerById_NotFound() {
        UUID randomId = UUID.randomUUID();
        when(customerRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> customerService.findCustomerById(randomId));
    }

    @Test
    @DisplayName("Search Customer - Returns Matching List")
    void testSearchCustomer_ReturnsList() {
        when(customerRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(
                "Khach", "Khach", "Khach"))
                .thenReturn(List.of(customer));

        List<Customer> result = customerService.searchCustomer("Khach");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(customer.getFullName(), result.get(0).getFullName());
    }
}
