package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.models.*;
import com.bluesky.pos_system.payload.dto.OrderDTO;
import com.bluesky.pos_system.payload.dto.OrderItemDTO;
import com.bluesky.pos_system.repositories.*;
import com.bluesky.pos_system.services.UserService;
import com.bluesky.pos_system.services.impl.OrderServiceImpl;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Branch branch;
    private Customer customer;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        branch = Branch.builder().id(UUID.randomUUID()).name("Main Branch").build();
        customer = Customer.builder()
                .id(UUID.randomUUID())
                .fullName("Nguyen Van A")
                .loyaltyPoints(10)
                .totalSpent(500000.0)
                .build();

        product = Product.builder()
                .id(UUID.randomUUID())
                .name("Bánh mì chảo")
                .sellingPrice(50000.0)
                .costPrice(25000.0)
                .build();

        inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .product(product)
                .branch(branch)
                .quantity(20)
                .build();
    }

    @Test
    @DisplayName("Create Order - Deducts inventory stock and awards loyalty points")
    void testCreateOrder_Success() {
        OrderItemDTO itemDTO = OrderItemDTO.builder()
                .productId(product.getId())
                .quantity(2)
                .build();

        OrderDTO orderRequest = OrderDTO.builder()
                .branchId(branch.getId())
                .customerId(customer.getId())
                .paymentType(PaymentType.CASH)
                .discount(10000.0)
                .tax(8000.0)
                .items(List.of(itemDTO))
                .build();

        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId())).thenReturn(inventory);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        OrderDTO result = orderService.createOrder(orderRequest);

        assertNotNull(result);
        // Subtotal = 2 * 50k = 100k
        // Total = 100k - 10k + 8k = 98k
        assertEquals(100000.0, result.getSubtotal());
        assertEquals(98000.0, result.getTotalAmount());
        assertEquals(OrderStatus.COMPLETED, result.getStatus());

        // Verify inventory deducted from 20 down to 18
        assertEquals(18, inventory.getQuantity());
        verify(inventoryRepository, times(1)).save(inventory);

        // Verify customer loyalty points updated
        // 98k / 10k = 9 points earned -> 10 + 9 = 19
        assertEquals(19, customer.getLoyaltyPoints());
        assertEquals(598000.0, customer.getTotalSpent());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("Create Order - Throws InsufficientStockException when requested quantity exceeds available stock")
    void testCreateOrder_ThrowsInsufficientStockException() {
        OrderItemDTO itemDTO = OrderItemDTO.builder()
                .productId(product.getId())
                .quantity(50) // More than available 20
                .build();

        OrderDTO orderRequest = OrderDTO.builder()
                .branchId(branch.getId())
                .customerId(customer.getId())
                .items(List.of(itemDTO))
                .build();

        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndBranchId(product.getId(), branch.getId())).thenReturn(inventory);

        assertThrows(com.bluesky.pos_system.exceptions.InsufficientStockException.class, () -> {
            orderService.createOrder(orderRequest);
        });

        // Verify stock was not deducted
        assertEquals(20, inventory.getQuantity());
        verify(orderRepository, never()).save(any());
    }
}
