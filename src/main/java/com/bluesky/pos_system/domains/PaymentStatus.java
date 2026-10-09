package com.bluesky.pos_system.domains;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
