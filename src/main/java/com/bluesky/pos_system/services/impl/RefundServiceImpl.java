package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.mappers.RefundMapper;
import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.models.Refund;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.RefundDTO;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.repositories.RefundRepository;
import com.bluesky.pos_system.services.RefundService;
import com.bluesky.pos_system.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefundServiceImpl implements RefundService {
    RefundRepository refundRepository;
    OrderRepository orderRepository;
    UserService userService;

    @Override
    public RefundDTO createRefund(RefundDTO refundDTO) {
        User cashier = userService.getCurrentUser();
        Order order = orderRepository.findById(refundDTO.getOrderId()).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy đơn hàng")
        );
        Branch branch = order.getBranch();
        Refund refund = Refund.builder()
                .order(order)
                .cashier(cashier)
                .branch(branch)
                .reason(refundDTO.getReason())
                .amount(refundDTO.getAmount())
                .build();
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
    public List<RefundDTO> getRefundByCashierIdAndDateRange(UUID customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return refundRepository.findByCashierIdAndCreatedAtBetween(customerId, startDate, endDate).stream().map(RefundMapper::toDTO).toList();
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
