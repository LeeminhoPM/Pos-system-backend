package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Refund;
import com.bluesky.pos_system.payload.dto.RefundDTO;

public class RefundMapper {
    public static RefundDTO toDTO(Refund refund) {
        if (refund == null) return null;

        return RefundDTO.builder()
                .id(refund.getId())
                .orderId(refund.getOrder() != null ? refund.getOrder().getId() : null)
                .reason(refund.getReason())
                .amount(refund.getAmount())
                .cashierName(refund.getCashier() != null ? refund.getCashier().getFullName() : null)
                .branchId(refund.getBranch() != null ? refund.getBranch().getId() : null)
                .shiftReportId(refund.getShiftReport() != null ? refund.getShiftReport().getId() : null)
                .createdAt(refund.getCreatedAt())
                .build();
    }
}
