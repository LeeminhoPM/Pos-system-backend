package com.bluesky.pos_system.mappers;

import com.bluesky.pos_system.models.Refund;
import com.bluesky.pos_system.payload.dto.RefundDTO;

public class RefundMapper {
    public static RefundDTO toDTO(Refund refund) {
        return  RefundDTO.builder()
                .id(refund.getId())
                .orderId(refund.getOrder().getId())
                .reason(refund.getReason())
                .amount(refund.getAmount())
                .cashierName(refund.getCashier().getFullName())
                .branchId(refund.getBranch().getId())
                .shiftReportId(refund.getShiftReport() != null ? refund.getShiftReport().getId() : null)
                .createdAt(refund.getCreatedAt())
                .build();
    }
}
