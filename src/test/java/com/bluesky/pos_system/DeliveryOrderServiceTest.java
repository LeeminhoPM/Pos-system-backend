package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.DeliveryStatus;
import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.DeliveryOrder;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.models.Product;
import com.bluesky.pos_system.payload.dto.CreateOnlineOrderRequestDTO;
import com.bluesky.pos_system.payload.dto.DeliveryOrderDTO;
import com.bluesky.pos_system.payload.dto.OrderItemDTO;
import com.bluesky.pos_system.payload.dto.UpdateDeliveryStatusRequestDTO;
import com.bluesky.pos_system.repositories.*;
import com.bluesky.pos_system.services.impl.DeliveryOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryOrderServiceTest {

    @Mock
    private DeliveryOrderRepository deliveryOrderRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private DeliveryOrderServiceImpl deliveryOrderService;

    private Branch branch;
    private Product product;

    @BeforeEach
    void setUp() {
        branch = Branch.builder().id(UUID.randomUUID()).name("Chi nhánh 1").build();
        product = Product.builder().id(UUID.randomUUID()).name("Cà phê sữa đá").sellingPrice(35000.0).build();
    }

    @Test
    void createOnlineDeliveryOrder_success() {
        CreateOnlineOrderRequestDTO request = CreateOnlineOrderRequestDTO.builder()
                .branchId(branch.getId())
                .recipientName("Tran Thi B")
                .recipientPhone("0912345678")
                .shippingAddress("123 Le Loi, Quan 1, TP.HCM")
                .shippingFee(20000.0)
                .items(List.of(OrderItemDTO.builder().productId(product.getId()).quantity(2).build()))
                .build();

        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });
        when(deliveryOrderRepository.save(any(DeliveryOrder.class))).thenAnswer(i -> {
            DeliveryOrder d = i.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DeliveryOrderDTO result = deliveryOrderService.createOnlineDeliveryOrder(request);

        assertNotNull(result);
        assertEquals("Tran Thi B", result.getRecipientName());
        assertEquals(90000.0, result.getTotalAmount()); // 35k * 2 + 20k ship = 90k
        assertEquals(DeliveryStatus.PENDING, result.getDeliveryStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(deliveryOrderRepository, times(1)).save(any(DeliveryOrder.class));
    }

    @Test
    void updateDeliveryStatus_toDelivered_completesOrder() {
        Order order = Order.builder().id(UUID.randomUUID()).status(OrderStatus.PENDING).totalAmount(100000.0).build();
        DeliveryOrder delivery = DeliveryOrder.builder()
                .id(UUID.randomUUID())
                .order(order)
                .deliveryStatus(DeliveryStatus.SHIPPING)
                .build();

        UpdateDeliveryStatusRequestDTO request = UpdateDeliveryStatusRequestDTO.builder()
                .status(DeliveryStatus.DELIVERED)
                .notes("Khách đã nhận hàng")
                .build();

        when(deliveryOrderRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));
        when(deliveryOrderRepository.save(any(DeliveryOrder.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryOrderDTO result = deliveryOrderService.updateDeliveryStatus(delivery.getId(), request);

        assertNotNull(result);
        assertEquals(DeliveryStatus.DELIVERED, result.getDeliveryStatus());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
}
