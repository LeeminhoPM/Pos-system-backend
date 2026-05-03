package com.bluesky.pos_system.services;

import com.bluesky.pos_system.payload.dto.RefundDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RefundService {
    RefundDTO createRefund(RefundDTO refundDTO);

    List<RefundDTO> getAllRefunds();

    List<RefundDTO> getRefundByCashierId(UUID cashierId);

    List<RefundDTO> getRefundByShiftReportId(UUID shiftReportId);

    List<RefundDTO> getRefundByCashierIdAndDateRange(UUID customerId, LocalDateTime startDate, LocalDateTime endDate);

    List<RefundDTO> getRefundByBranchId(UUID branchId);

    RefundDTO getRefundById(UUID id);

    void deleteRefund(UUID id);
}
