package com.bluesky.pos_system;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.models.PaymentTransaction;
import com.bluesky.pos_system.payload.dto.*;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.repositories.PaymentTransactionRepository;
import com.bluesky.pos_system.services.LoyaltyService;
import com.bluesky.pos_system.services.impl.VietnamPaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VietnamPaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private LoyaltyService loyaltyService;

    @InjectMocks
    private VietnamPaymentServiceImpl vietnamPaymentService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(UUID.randomUUID())
                .orderNumber("ORD-20261012-9999")
                .totalAmount(250000.0)
                .status(OrderStatus.PENDING)
                .build();
    }

    @Test
    void createVietQRPayment_success() {
        VietQRPaymentRequestDTO request = VietQRPaymentRequestDTO.builder()
                .orderId(order.getId())
                .bankId("MB")
                .accountNumber("0987654321")
                .accountName("STORE POS")
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> {
            PaymentTransaction pt = i.getArgument(0);
            pt.setId(UUID.randomUUID());
            return pt;
        });

        VietQRPaymentResponseDTO response = vietnamPaymentService.createVietQRPayment(request);

        assertNotNull(response);
        assertEquals(order.getId(), response.getOrderId());
        assertEquals(250000.0, response.getAmount());
        assertTrue(response.getQrCodeUrl().contains("img.vietqr.io"));
        verify(paymentTransactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    void createEWalletPayment_momo_success() {
        EWalletPaymentRequestDTO request = EWalletPaymentRequestDTO.builder()
                .orderId(order.getId())
                .walletType(PaymentType.MOMO)
                .build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> {
            PaymentTransaction pt = i.getArgument(0);
            pt.setId(UUID.randomUUID());
            return pt;
        });

        EWalletPaymentResponseDTO response = vietnamPaymentService.createEWalletPayment(request);

        assertNotNull(response);
        assertEquals(PaymentType.MOMO, response.getWalletType());
        assertNotNull(response.getPayUrl());
        verify(paymentTransactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    void confirmPayment_success() {
        PaymentTransaction txn = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .transactionCode("TXN-VQR-12345")
                .order(order)
                .amount(250000.0)
                .paymentType(PaymentType.BANK_TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentConfirmRequestDTO request = PaymentConfirmRequestDTO.builder()
                .transactionId(txn.getId())
                .bankTransactionReference("FT26101234567")
                .notes("Đã nhận tiền qua MBBank")
                .build();

        when(paymentTransactionRepository.findById(txn.getId())).thenReturn(Optional.of(txn));
        when(paymentTransactionRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        PaymentResponseDTO response = vietnamPaymentService.confirmPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS.name(), response.getStatus());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
}
