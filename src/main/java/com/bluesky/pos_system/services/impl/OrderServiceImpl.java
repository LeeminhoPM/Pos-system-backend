package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.mappers.OrderMapper;
import com.bluesky.pos_system.models.*;
import com.bluesky.pos_system.payload.dto.OrderDTO;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.repositories.ProductRepository;
import com.bluesky.pos_system.services.OrderService;
import com.bluesky.pos_system.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    ProductRepository productRepository;
    UserService userService;

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        User cashier = userService.getCurrentUser();
        Branch branch = cashier.getBranch();
        if (branch == null) {
            throw new RuntimeException("Không tìm thấy branch");
        }

        Order order = Order.builder()
                .branch(branch)
                .cashier(cashier)
                .customer(orderDTO.getCustomer())
                .paymentType(orderDTO.getPaymentType())
                .build();

        List<OrderItem> orderItems = orderDTO.getItems().stream().map(
                orderItemDTO -> {
                    Product product = productRepository.findById(orderItemDTO.getProductId()).orElseThrow(
                            () -> new EntityNotFoundException("Không tìm thấy sản phẩm")
                    );
                    return OrderItem.builder()
                            .product(product)
                            .quantity(orderItemDTO.getQuantity())
                            .price(product.getSellingPrice() * orderItemDTO.getQuantity())
                            .order(order)
                            .build();
                }
        ).toList();
        double total = orderItems.stream().mapToDouble(OrderItem::getPrice).sum();
        order.setTotalAmount(total);
        order.setItems(orderItems);

        return OrderMapper.toDTO(orderRepository.save(order));
    }

    @Override
    public void deleteOrder(UUID id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy sản phẩm")
        );
        orderRepository.delete(order);
    }

    @Override
    public OrderDTO getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy đơn hàng")
        );
        return OrderMapper.toDTO(order);
    }

    @Override
    public List<OrderDTO> getOrderByBranch(UUID branchId, UUID customerId, UUID cashierId, PaymentType paymentType, OrderStatus orderStatus) {
        List<Order> orders = orderRepository.findByBranchId(branchId).stream().filter(
                order -> customerId == null ||
                        (order.getCustomer() != null && order.getCustomer().getId().equals(customerId))
        ).filter(
                order -> cashierId == null ||
                        (order.getCashier() != null && order.getCashier().getId().equals(cashierId))
        ).filter(
                order -> paymentType == null || order.getPaymentType() == paymentType
        ).toList();
        return orders.stream().map(OrderMapper::toDTO).toList();
    }

    @Override
    public List<OrderDTO> getOrderByCashier(UUID cashierId) {
        return orderRepository.findByCashierId(cashierId).stream().map(OrderMapper::toDTO).toList();
    }

    @Override
    public List<OrderDTO> getOrderByCustomer(UUID customerId) {
        return orderRepository.findByCustomerId(customerId).stream().map(OrderMapper::toDTO).toList();
    }

    @Override
    public List<OrderDTO> getTodayOrderByBranch(UUID branchId) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        return orderRepository.findByBranchIdAndCreatedAtBetween(branchId, todayStart, todayEnd).stream().map(OrderMapper::toDTO).toList();
    }

    @Override
    public List<OrderDTO> getTop5RecentOrderByBranch(UUID branchId) {
        return orderRepository.findTop5ByBranchIdOrderByCreatedAtDesc(branchId).stream().map(OrderMapper::toDTO).toList();
    }
}
