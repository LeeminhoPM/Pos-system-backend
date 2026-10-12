package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.models.PaymentTransaction;
import com.bluesky.pos_system.payload.dto.*;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.repositories.PaymentTransactionRepository;
import com.bluesky.pos_system.services.LoyaltyService;
import com.bluesky.pos_system.services.VietnamPaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VietnamPaymentServiceImpl implements VietnamPaymentService {
    OrderRepository orderRepository;
    PaymentTransactionRepository paymentTransactionRepository;
    LoyaltyService loyaltyService;

    // Cấu hình tài khoản ngân hàng mặc định cho POS
    private static final String DEFAULT_BANK_ID = "MB"; // Ngân hàng Quân Đội MBBank
    private static final String DEFAULT_ACCOUNT_NO = "098765432101";
    private static final String DEFAULT_ACCOUNT_NAME = "SKYPOS STORE SYSTEM";

    @Override
    @Transactional
    public VietQRPaymentResponseDTO createVietQRPayment(VietQRPaymentRequestDTO request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với id: " + request.getOrderId()));

        String bankId = (request.getBankId() != null && !request.getBankId().isBlank()) ? request.getBankId() : DEFAULT_BANK_ID;
        String accountNo = (request.getAccountNumber() != null && !request.getAccountNumber().isBlank()) ? request.getAccountNumber() : DEFAULT_ACCOUNT_NO;
        String accountName = (request.getAccountName() != null && !request.getAccountName().isBlank()) ? request.getAccountName() : DEFAULT_ACCOUNT_NAME;

        String transferContent = "POS " + order.getOrderNumber();
        long amount = Math.round(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);

        String encodedDesc = URLEncoder.encode(transferContent, StandardCharsets.UTF_8);
        String encodedAccountName = URLEncoder.encode(accountName, StandardCharsets.UTF_8);

        // Chuẩn VietQR API link tạo mã QR động
        String qrCodeUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                bankId, accountNo, amount, encodedDesc, encodedAccountName);
        String quickLink = String.format("https://api.vietqr.io/%s/%s/%d/%s/vietqr_net_2.jpg?accountName=%s",
                bankId, accountNo, amount, encodedDesc, encodedAccountName);

        String txnCode = "TXN-VQR-" + System.currentTimeMillis() % 10000000;

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionCode(txnCode)
                .order(order)
                .amount((double) amount)
                .paymentType(PaymentType.BANK_TRANSFER)
                .status(PaymentStatus.PENDING)
                .gatewayReference(bankId + "-" + accountNo)
                .currency("vnd")
                .notes("Chuyển khoản ngân hàng VietQR: " + transferContent)
                .createdAt(LocalDateTime.now())
                .build();

        PaymentTransaction savedTxn = paymentTransactionRepository.save(transaction);
        log.info("Created VietQR transaction: txnCode={}, orderNumber={}, amount={}", txnCode, order.getOrderNumber(), amount);

        return VietQRPaymentResponseDTO.builder()
                .transactionId(savedTxn.getId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .amount((double) amount)
                .bankId(bankId)
                .accountNumber(accountNo)
                .accountName(accountName)
                .transferContent(transferContent)
                .qrCodeUrl(qrCodeUrl)
                .quickLink(quickLink)
                .status(PaymentStatus.PENDING.name())
                .build();
    }

    @Override
    @Transactional
    public EWalletPaymentResponseDTO createEWalletPayment(EWalletPaymentRequestDTO request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với id: " + request.getOrderId()));

        PaymentType walletType = request.getWalletType();
        if (walletType != PaymentType.MOMO && walletType != PaymentType.ZALOPAY) {
            throw new IllegalArgumentException("Chỉ hỗ trợ ví điện tử MOMO hoặc ZALOPAY");
        }

        long amount = Math.round(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
        String prefix = walletType == PaymentType.MOMO ? "MOMO" : "ZLP";
        String txnCode = "TXN-" + prefix + "-" + System.currentTimeMillis() % 10000000;

        // URL thanh toán (Sandbox simulator / Production gateway URL)
        String gatewayRef = prefix.toLowerCase() + "_order_" + UUID.randomUUID().toString().substring(0, 8);
        String payUrl = String.format("https://payment.skypos.vn/%s/pay?txn=%s&amount=%d", prefix.toLowerCase(), txnCode, amount);
        String qrCodeUrl = String.format("https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=%s", URLEncoder.encode(payUrl, StandardCharsets.UTF_8));
        String deeplink = String.format("%s://pay?appId=skypos&orderId=%s&amount=%d", prefix.toLowerCase(), order.getOrderNumber(), amount);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionCode(txnCode)
                .order(order)
                .amount((double) amount)
                .paymentType(walletType)
                .status(PaymentStatus.PENDING)
                .gatewayReference(gatewayRef)
                .currency("vnd")
                .notes("Thanh toán ví điện tử " + walletType.name() + " qua POS")
                .createdAt(LocalDateTime.now())
                .build();

        PaymentTransaction savedTxn = paymentTransactionRepository.save(transaction);
        log.info("Created {} transaction: txnCode={}, orderNumber={}, amount={}", walletType, txnCode, order.getOrderNumber(), amount);

        return EWalletPaymentResponseDTO.builder()
                .transactionId(savedTxn.getId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .walletType(walletType)
                .amount((double) amount)
                .payUrl(payUrl)
                .qrCodeUrl(qrCodeUrl)
                .deeplink(deeplink)
                .status(PaymentStatus.PENDING.name())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponseDTO confirmPayment(PaymentConfirmRequestDTO request) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giao dịch thanh toán"));

        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            return mapToDTO(transaction);
        }

        transaction.setStatus(PaymentStatus.SUCCESS);
        if (request.getBankTransactionReference() != null && !request.getBankTransactionReference().isBlank()) {
            transaction.setChargeId(request.getBankTransactionReference());
        }
        if (request.getNotes() != null) {
            transaction.setNotes(transaction.getNotes() + " | " + request.getNotes());
        }
        PaymentTransaction updatedTxn = paymentTransactionRepository.save(transaction);

        // Update Order status
        Order order = transaction.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setPaymentType(transaction.getPaymentType());
            orderRepository.save(order);

            // Tích điểm thưởng cho khách hàng nếu có
            if (order.getCustomer() != null) {
                try {
                    loyaltyService.earnPoints(order.getCustomer(), order, order.getTotalAmount());
                } catch (Exception e) {
                    log.warn("Error accumulating loyalty points during payment confirmation: {}", e.getMessage());
                }
            }
        }

        log.info("Payment confirmed successfully: txnCode={}, orderNumber={}", transaction.getTransactionCode(), order != null ? order.getOrderNumber() : "N/A");
        return mapToDTO(updatedTxn);
    }

    @Override
    @Transactional
    public PaymentResponseDTO processWebhook(String gateway, Map<String, Object> webhookData) {
        log.info("Processing webhook from gateway {}: {}", gateway, webhookData);
        String txnCode = (String) webhookData.get("transactionCode");
        if (txnCode == null) {
            txnCode = (String) webhookData.get("orderId");
        }

        PaymentTransaction transaction = null;
        if (txnCode != null) {
            transaction = paymentTransactionRepository.findByTransactionCode(txnCode).orElse(null);
        }
        if (transaction == null) {
            String gatewayRef = (String) webhookData.get("transId");
            if (gatewayRef != null) {
                transaction = paymentTransactionRepository.findByGatewayReference(gatewayRef).orElse(null);
            }
        }

        if (transaction == null) {
            throw new EntityNotFoundException("Không tìm thấy giao dịch tương ứng với webhook: " + txnCode);
        }

        transaction.setStatus(PaymentStatus.SUCCESS);
        transaction.setUpdatedAt(LocalDateTime.now());
        PaymentTransaction savedTxn = paymentTransactionRepository.save(transaction);

        Order order = transaction.getOrder();
        if (order != null && order.getStatus() != OrderStatus.COMPLETED) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            if (order.getCustomer() != null) {
                try {
                    loyaltyService.earnPoints(order.getCustomer(), order, order.getTotalAmount());
                } catch (Exception e) {
                    log.warn("Error auto accumulating loyalty points in webhook: {}", e.getMessage());
                }
            }
        }

        return mapToDTO(savedTxn);
    }

    @Override
    public PaymentResponseDTO getTransactionStatus(UUID transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giao dịch thanh toán"));
        return mapToDTO(transaction);
    }

    private PaymentResponseDTO mapToDTO(PaymentTransaction pt) {
        return PaymentResponseDTO.builder()
                .id(pt.getId())
                .transactionId(pt.getTransactionCode())
                .orderId(pt.getOrder() != null ? pt.getOrder().getId() : null)
                .orderNumber(pt.getOrder() != null ? pt.getOrder().getOrderNumber() : null)
                .paymentMethod(pt.getPaymentType() != null ? pt.getPaymentType().name() : null)
                .status(pt.getStatus() != null ? pt.getStatus().name() : null)
                .amount(pt.getAmount())
                .currency(pt.getCurrency())
                .gatewayReference(pt.getGatewayReference())
                .chargeId(pt.getChargeId())
                .receiptUrl(pt.getReceiptUrl())
                .notes(pt.getNotes())
                .createdAt(pt.getCreatedAt())
                .updatedAt(pt.getUpdatedAt())
                .build();
    }
}
