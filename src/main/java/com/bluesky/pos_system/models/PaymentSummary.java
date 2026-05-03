package com.bluesky.pos_system.models;

import com.bluesky.pos_system.domains.PaymentType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentSummary {
    PaymentType paymentType;

    Double totalAmount;

    int transactionCount;

    double percentage;
}