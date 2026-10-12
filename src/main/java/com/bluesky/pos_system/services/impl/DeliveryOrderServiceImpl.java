package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.DeliveryStatus;
import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.models.*;
import com.bluesky.pos_system.payload.dto.CreateOnlineOrderRequestDTO;
import com.bluesky.pos_system.payload.dto.DeliveryOrderDTO;
import com.bluesky.pos_system.payload.dto.PageResponse;
import com.bluesky.pos_system.payload.dto.UpdateDeliveryStatusRequestDTO;
import com.bluesky.pos_system.repositories.*;
import com.bluesky.pos_system.services.DeliveryOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeliveryOrderServiceImpl implements DeliveryOrderService {
    DeliveryOrderRepository deliveryOrderRepository;
    OrderRepository orderRepository;
    ProductRepository productRepository;
    CustomerRepository customerRepository;
    BranchRepository branchRepository;
    InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public DeliveryOrderDTO createOnlineDeliveryOrder(CreateOnlineOrderRequestDTO request) {
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chi nhánh với id: " + request.getBranchId()));

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId()).orElse(null);
        }

        String orderNumber = "ONL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", (int) (Math.random() * 10000));

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .branch(branch)
                .customer(customer)
                .paymentType(request.getPaymentType() != null ? request.getPaymentType() : PaymentType.CASH)
                .status(OrderStatus.PENDING)
                .notes("Đơn hàng online giao tận nơi. " + (request.getNotes() != null ? request.getNotes() : ""))
                .build();

        List<OrderItem> items = request.getItems().stream().map(itemDTO -> {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với id: " + itemDTO.getProductId()));
            int qty = itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 1;
            double price = (product.getSellingPrice() != null ? product.getSellingPrice() : 0.0) * qty;

            return OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(qty)
                    .price(price)
                    .build();
        }).toList();

        double subtotal = items.stream().mapToDouble(OrderItem::getPrice).sum();
        double shippingFee = request.getShippingFee() != null ? request.getShippingFee() : 0.0;
        double total = subtotal + shippingFee;

        order.setSubtotal(subtotal);
        order.setDiscount(0.0);
        order.setTax(0.0);
        order.setTotalAmount(total);
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        String trackingNumber = "TRK-" + System.currentTimeMillis() % 10000000;
        DeliveryOrder deliveryOrder = DeliveryOrder.builder()
                .order(savedOrder)
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .shippingAddress(request.getShippingAddress())
                .shippingFee(shippingFee)
                .trackingNumber(trackingNumber)
                .deliveryCarrier(request.getDeliveryCarrier() != null ? request.getDeliveryCarrier() : "Nội bộ")
                .deliveryStatus(DeliveryStatus.PENDING)
                .estimatedDeliveryTime(LocalDateTime.now().plusHours(2))
                .notes(request.getNotes())
                .build();

        DeliveryOrder savedDelivery = deliveryOrderRepository.save(deliveryOrder);
        log.info("Created online delivery order: orderNumber={}, trackingNumber={}, total={}",
                savedOrder.getOrderNumber(), trackingNumber, total);

        return mapToDTO(savedDelivery);
    }

    @Override
    @Transactional
    public DeliveryOrderDTO updateDeliveryStatus(UUID deliveryOrderId, UpdateDeliveryStatusRequestDTO request) {
        DeliveryOrder deliveryOrder = deliveryOrderRepository.findById(deliveryOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn vận chuyển"));

        deliveryOrder.setDeliveryStatus(request.getStatus());
        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isBlank()) {
            deliveryOrder.setTrackingNumber(request.getTrackingNumber());
        }
        if (request.getNotes() != null) {
            deliveryOrder.setNotes(request.getNotes());
        }

        Order order = deliveryOrder.getOrder();
        if (request.getStatus() == DeliveryStatus.DELIVERED) {
            deliveryOrder.setDeliveredAt(LocalDateTime.now());
            if (order != null) {
                order.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
            }
        } else if (request.getStatus() == DeliveryStatus.CANCELLED) {
            if (order != null) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        }

        DeliveryOrder updated = deliveryOrderRepository.save(deliveryOrder);
        log.info("Updated delivery status for {}: newStatus={}", deliveryOrderId, request.getStatus());
        return mapToDTO(updated);
    }

    @Override
    public DeliveryOrderDTO trackDelivery(String trackingNumber) {
        DeliveryOrder delivery = deliveryOrderRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy mã vận đơn: " + trackingNumber));
        return mapToDTO(delivery);
    }

    @Override
    public DeliveryOrderDTO getDeliveryOrderById(UUID deliveryOrderId) {
        DeliveryOrder delivery = deliveryOrderRepository.findById(deliveryOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn vận chuyển"));
        return mapToDTO(delivery);
    }

    @Override
    public PageResponse<DeliveryOrderDTO> getDeliveryOrdersByBranch(UUID branchId, DeliveryStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DeliveryOrder> pageResult;
        if (status != null) {
            pageResult = deliveryOrderRepository.findByOrder_Branch_IdAndDeliveryStatus(branchId, status, pageRequest);
        } else {
            pageResult = deliveryOrderRepository.findByOrder_Branch_Id(branchId, pageRequest);
        }

        List<DeliveryOrderDTO> list = pageResult.getContent().stream().map(this::mapToDTO).toList();
        return PageResponse.<DeliveryOrderDTO>builder()
                .content(list)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .isLast(pageResult.isLast())
                .build();
    }

    private DeliveryOrderDTO mapToDTO(DeliveryOrder d) {
        return DeliveryOrderDTO.builder()
                .id(d.getId())
                .orderId(d.getOrder() != null ? d.getOrder().getId() : null)
                .orderNumber(d.getOrder() != null ? d.getOrder().getOrderNumber() : null)
                .totalAmount(d.getOrder() != null && d.getOrder().getTotalAmount() != null ? d.getOrder().getTotalAmount() : 0.0)
                .recipientName(d.getRecipientName())
                .recipientPhone(d.getRecipientPhone())
                .shippingAddress(d.getShippingAddress())
                .shippingFee(d.getShippingFee())
                .trackingNumber(d.getTrackingNumber())
                .deliveryCarrier(d.getDeliveryCarrier())
                .deliveryStatus(d.getDeliveryStatus())
                .estimatedDeliveryTime(d.getEstimatedDeliveryTime())
                .deliveredAt(d.getDeliveredAt())
                .notes(d.getNotes())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
