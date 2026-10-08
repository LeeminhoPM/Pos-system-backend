package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.mappers.RefundMapper;
import com.bluesky.pos_system.models.*;
import com.bluesky.pos_system.payload.dto.RefundDTO;
import com.bluesky.pos_system.repositories.*;
import com.bluesky.pos_system.services.RefundService;
import com.bluesky.pos_system.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefundServiceImpl implements RefundService {
    RefundRepository refundRepository;
    OrderRepository orderRepository;
    InventoryRepository inventoryRepository;
    ShiftReportRepository shiftReportRepository;
    UserService userService;

    @Override
    @Transactional
    public RefundDTO createRefund(RefundDTO refundDTO) {
        User cashier = null;
        try {
            cashier = userService.getCurrentUser();
        } catch (Exception ignored) {
        }

        Order order = orderRepository.findById(refundDTO.getOrderId()).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy đơn hàng với id: " + refundDTO.getOrderId())
        );
        Branch branch = order.getBranch();

        // Check if cashier has an ongoing shift report to link
        ShiftReport shiftReport = null;
        if (cashier != null) {
            Optional<ShiftReport> activeShift = shiftReportRepository.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(cashier);
            if (activeShift.isPresent()) {
                shiftReport = activeShift.get();
            }
        }

        Refund refund = Refund.builder()
                .order(order)
                .cashier(cashier != null ? cashier : order.getCashier())
                .branch(branch)
                .shiftReport(shiftReport)
                .reason(refundDTO.getReason())
                .amount(refundDTO.getAmount() != null ? refundDTO.getAmount() : order.getTotalAmount())
                .paymentType(order.getPaymentType())
                .build();

        // Update Order status
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        // Restock inventory for items in this order
        if (branch != null && order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Inventory inv = inventoryRepository.findByProductIdAndBranchId(item.getProduct().getId(), branch.getId());
                    if (inv != null) {
                        inv.setQuantity(inv.getQuantity() + (item.getQuantity() != null ? item.getQuantity() : 1));
                        inventoryRepository.save(inv);
                    }
                }
            }
        }

        return RefundMapper.toDTO(refundRepository.save(refund));
    }

    @Override
    public List<RefundDTO> getAllRefunds() {
        return refundRepository.findAll().stream().map(RefundMapper::toDTO).toList();
    }

    @Override
    public List<RefundDTO> getRefundByCashierId(UUID cashierId) {
        return refundRepository.findByCashierId(cashierId).stream().map(RefundMapper::toDTO).toList();
    }

    @Override
    public List<RefundDTO> getRefundByShiftReportId(UUID shiftReportId) {
        return refundRepository.findByShiftReportId(shiftReportId).stream().map(RefundMapper::toDTO).toList();
    }

    @Override
    public List<RefundDTO> getRefundByCashierIdAndDateRange(UUID cashierId, LocalDateTime startDate, LocalDateTime endDate) {
        return refundRepository.findByCashierIdAndCreatedAtBetween(cashierId, startDate, endDate).stream().map(RefundMapper::toDTO).toList();
    }

    @Override
    public List<RefundDTO> getRefundByBranchId(UUID branchId) {
        return refundRepository.findByBranchId(branchId).stream().map(RefundMapper::toDTO).toList();
    }

    @Override
    public RefundDTO getRefundById(UUID id) {
        Refund refund = refundRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy đơn hoàn")
        );
        return RefundMapper.toDTO(refund);
    }

    @Override
    public void deleteRefund(UUID id) {
        Refund refund = refundRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy đơn hoàn")
        );
        refundRepository.delete(refund);
    }
}
