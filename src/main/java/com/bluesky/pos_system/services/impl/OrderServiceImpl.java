package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.mappers.OrderMapper;
import com.bluesky.pos_system.models.*;
import com.bluesky.pos_system.payload.dto.OrderDTO;
import com.bluesky.pos_system.repositories.*;
import com.bluesky.pos_system.services.OrderService;
import com.bluesky.pos_system.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {
    OrderRepository orderRepository;
    ProductRepository productRepository;
    CustomerRepository customerRepository;
    InventoryRepository inventoryRepository;
    InventoryTransactionRepository inventoryTransactionRepository;
    BranchRepository branchRepository;
    UserService userService;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        User cashier = null;
        try {
            cashier = userService.getCurrentUser();
        } catch (Exception ignored) {
        }

        Branch branch = null;
        if (orderDTO.getBranchId() != null) {
            branch = branchRepository.findById(orderDTO.getBranchId()).orElse(null);
        }
        if (branch == null && cashier != null) {
            branch = cashier.getBranch();
        }
        if (branch == null) {
            throw new RuntimeException("Không tìm thấy chi nhánh hợp lệ để tạo đơn hàng");
        }

        Customer customer = null;
        if (orderDTO.getCustomerId() != null) {
            customer = customerRepository.findById(orderDTO.getCustomerId()).orElse(null);
        } else if (orderDTO.getCustomer() != null && orderDTO.getCustomer().getId() != null) {
            customer = customerRepository.findById(orderDTO.getCustomer().getId()).orElse(null);
        }

        String orderNumber = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", (int)(Math.random() * 10000));

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .branch(branch)
                .cashier(cashier)
                .customer(customer)
                .paymentType(orderDTO.getPaymentType() != null ? orderDTO.getPaymentType() : PaymentType.CASH)
                .status(orderDTO.getStatus() != null ? orderDTO.getStatus() : OrderStatus.COMPLETED)
                .notes(orderDTO.getNotes())
                .build();

        final Branch finalBranch = branch;
        final User finalCashier = cashier;
        List<OrderItem> orderItems = orderDTO.getItems().stream().map(
                orderItemDTO -> {
                    Product product = productRepository.findById(orderItemDTO.getProductId()).orElseThrow(
                            () -> new EntityNotFoundException("Không tìm thấy sản phẩm với id: " + orderItemDTO.getProductId())
                    );
                    int qty = orderItemDTO.getQuantity() != null ? orderItemDTO.getQuantity() : 1;
                    double itemPrice = (product.getSellingPrice() != null ? product.getSellingPrice() : 0.0) * qty;

                    // Validate & deduct inventory stock with audit transaction
                    Inventory inventory = inventoryRepository.findByProductIdAndBranchId(product.getId(), finalBranch.getId());
                    int availableStock = (inventory != null && inventory.getQuantity() != null) ? inventory.getQuantity() : 0;
                    if (inventory == null || availableStock < qty) {
                        throw new com.bluesky.pos_system.exceptions.InsufficientStockException(
                                "Sản phẩm '" + product.getName() + "' (SKU: " + product.getSku() +
                                ") không đủ tồn kho tại chi nhánh " + finalBranch.getName() +
                                ". Tồn kho hiện có: " + availableStock + ", yêu cầu mua: " + qty
                        );
                    }

                    int remaining = availableStock - qty;
                    inventory.setQuantity(remaining);
                    inventoryRepository.save(inventory);

                    // Create stock audit trail entry
                    InventoryTransaction tx = InventoryTransaction.builder()
                            .branch(finalBranch)
                            .product(product)
                            .type(com.bluesky.pos_system.domains.InventoryTransactionType.SALE)
                            .quantityChange(-qty)
                            .balanceAfter(remaining)
                            .referenceNumber(orderNumber)
                            .notes("Bán lẻ qua đơn #" + orderNumber)
                            .createdBy(finalCashier)
                            .build();
                    inventoryTransactionRepository.save(tx);

                    return OrderItem.builder()
                            .product(product)
                            .quantity(qty)
                            .price(itemPrice)
                            .order(order)
                            .build();
                }
        ).toList();

        double subtotal = orderItems.stream().mapToDouble(OrderItem::getPrice).sum();
        double discount = orderDTO.getDiscount() != null ? orderDTO.getDiscount() : 0.0;
        double tax = orderDTO.getTax() != null ? orderDTO.getTax() : 0.0;
        double total = Math.max(0.0, subtotal - discount + tax);

        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setTax(tax);
        order.setTotalAmount(total);
        order.setItems(orderItems);

        // Update customer totalSpent & loyaltyPoints
        if (customer != null) {
            customer.setTotalSpent((customer.getTotalSpent() != null ? customer.getTotalSpent() : 0.0) + total);
            int earnedPoints = (int) (total / 10000.0);
            customer.setLoyaltyPoints((customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0) + earnedPoints);
            customerRepository.save(customer);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: orderNumber={}, branchId={}, totalAmount={}, itemsCount={}",
                savedOrder.getOrderNumber(), finalBranch.getId(), savedOrder.getTotalAmount(), orderItems.size());
        return OrderMapper.toDTO(savedOrder);
    }

    @Override
    public void deleteOrder(UUID id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy đơn hàng")
        );
        orderRepository.delete(order);
    }

    @Override
    public OrderDTO getOrderById(UUID orderId) {
        Order order = orderRepository.findWithDetailsById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy đơn hàng")
        );
        return OrderMapper.toDTO(order);
    }

    @Override
    public OrderDTO updateOrderStatus(UUID id, OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy đơn hàng")
        );
        order.setStatus(status);
        return OrderMapper.toDTO(orderRepository.save(order));
    }

    @Override
    public List<OrderDTO> getOrderByBranch(UUID branchId, UUID customerId, UUID cashierId, PaymentType paymentType, OrderStatus orderStatus) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(
                0, 200, org.springframework.data.domain.Sort.by("createdAt").descending());
        return orderRepository.findFilteredOrders(branchId, customerId, cashierId, paymentType, orderStatus, pageRequest)
                .getContent().stream()
                .map(OrderMapper::toDTO)
                .toList();
    }

    @Override
    public com.bluesky.pos_system.payload.dto.PageResponse<OrderDTO> getOrdersPaged(
            UUID branchId, UUID customerId, UUID cashierId, PaymentType paymentType, OrderStatus orderStatus, int page, int size) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<Order> orderPage = orderRepository.findFilteredOrders(
                branchId, customerId, cashierId, paymentType, orderStatus, pageRequest);
        List<OrderDTO> dtoList = orderPage.getContent().stream().map(OrderMapper::toDTO).toList();
        return com.bluesky.pos_system.payload.dto.PageResponse.<OrderDTO>builder()
                .content(dtoList)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .isLast(orderPage.isLast())
                .build();
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
