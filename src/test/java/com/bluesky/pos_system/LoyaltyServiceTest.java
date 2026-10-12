package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.CustomerTier;
import com.bluesky.pos_system.domains.LoyaltyTransactionType;
import com.bluesky.pos_system.models.Customer;
import com.bluesky.pos_system.models.CustomerLoyaltyTransaction;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.payload.dto.*;
import com.bluesky.pos_system.repositories.CustomerLoyaltyRepository;
import com.bluesky.pos_system.repositories.CustomerRepository;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.services.impl.LoyaltyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoyaltyServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerLoyaltyRepository loyaltyRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private LoyaltyServiceImpl loyaltyService;

    private Customer customer;
    private Order order;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(UUID.randomUUID())
                .customerCode("CUST-1001")
                .fullName("Nguyen Van A")
                .email("nguyenvana@gmail.com")
                .phone("0901234567")
                .tier(CustomerTier.BRONZE)
                .loyaltyPoints(100)
                .totalSpent(2000000.0)
                .build();

        order = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-20261012-0001")
                .totalAmount(500000.0)
                .build();
    }

    @Test
    void earnPoints_success_bronzeTier() {
        when(loyaltyRepository.save(any(CustomerLoyaltyTransaction.class))).thenAnswer(i -> {
            CustomerLoyaltyTransaction t = i.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        LoyaltyTransactionDTO result = loyaltyService.earnPoints(customer, order, 500000.0);

        assertNotNull(result);
        assertEquals(50, result.getPointsChange()); // 500k / 10k * 1.0 = 50 điểm
        assertEquals(150, result.getBalanceAfter());
        verify(customerRepository, times(1)).save(customer);
        verify(loyaltyRepository, times(1)).save(any(CustomerLoyaltyTransaction.class));
    }

    @Test
    void earnPoints_promotesToSilver_whenTotalSpentExceedsThreshold() {
        customer.setTotalSpent(6000000.0); // > 5,000,000 threshold
        when(loyaltyRepository.save(any(CustomerLoyaltyTransaction.class))).thenAnswer(i -> i.getArgument(0));

        loyaltyService.earnPoints(customer, order, 200000.0);

        assertEquals(CustomerTier.SILVER, customer.getTier());
    }

    @Test
    void redeemPoints_success() {
        RedeemPointsRequestDTO request = RedeemPointsRequestDTO.builder()
                .customerId(customer.getId())
                .pointsToRedeem(50)
                .build();

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(loyaltyRepository.save(any(CustomerLoyaltyTransaction.class))).thenAnswer(i -> i.getArgument(0));

        RedeemPointsResponseDTO response = loyaltyService.redeemPoints(request);

        assertNotNull(response);
        assertEquals(50, response.getRedeemedPoints());
        assertEquals(50000.0, response.getDiscountAmount()); // 50 * 1,000
        assertEquals(50, response.getRemainingPoints());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void redeemPoints_insufficientPoints_throwsException() {
        RedeemPointsRequestDTO request = RedeemPointsRequestDTO.builder()
                .customerId(customer.getId())
                .pointsToRedeem(200) // current is 100
                .build();

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        assertThrows(IllegalArgumentException.class, () -> loyaltyService.redeemPoints(request));
    }

    @Test
    void getCustomerCRMDetail_success() {
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(loyaltyRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())).thenReturn(Collections.emptyList());
        when(orderRepository.findByCustomerId(customer.getId())).thenReturn(List.of(order));

        CustomerCRMDetailDTO crm = loyaltyService.getCustomerCRMDetail(customer.getId());

        assertNotNull(crm);
        assertEquals(customer.getFullName(), crm.getFullName());
        assertEquals(1, crm.getTotalOrders());
        assertNotNull(crm.getSegmentName());
    }

    @Test
    void getCustomerSegmentSummary_success() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        CustomerSegmentSummaryDTO summary = loyaltyService.getCustomerSegmentSummary();

        assertNotNull(summary);
        assertEquals(1, summary.getTotalCustomers());
        assertEquals(1, summary.getBronzeCount());
        assertEquals(100.0, summary.getTotalLoyaltyPointsCirculating());
    }
}
