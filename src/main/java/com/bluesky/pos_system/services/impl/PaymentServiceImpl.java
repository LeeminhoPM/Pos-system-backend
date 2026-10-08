package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.models.PaymentTransaction;
import com.bluesky.pos_system.payload.dto.PaymentInitiateRequestDTO;
import com.bluesky.pos_system.payload.dto.PaymentResponseDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.repositories.PaymentTransactionRepository;
import com.bluesky.pos_system.services.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentServiceImpl implements PaymentService {

    final PaymentTransactionRepository transactionRepository;
    final OrderRepository orderRepository;
    final ObjectMapper objectMapper = new ObjectMapper();

    // Idempotency cache for processed webhook event IDs
    final Set<String> processedWebhookEvents = ConcurrentHashMap.newKeySet();

    @Value("${stripe.api.key:}")
    String stripeApiKey;

    @Value("${stripe.webhook.secret:}")
    String stripeWebhookSecret;

    @PostConstruct
    public void init() {
        if (stripeApiKey != null && !stripeApiKey.isBlank()) {
            Stripe.apiKey = stripeApiKey;
            log.info("Stripe payment gateway initialized successfully");
        } else {
            log.warn("Stripe API key is not configured. Falling back to sandbox simulation mode.");
        }
    }

    @Override
    @Transactional
    public PaymentResponseDTO initiatePayment(PaymentInitiateRequestDTO request) {
        Order order = orderRepository.findById(request.getOrderId()).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + request.getOrderId())
        );

        String transactionCode = "TXN-" + System.currentTimeMillis() % 10000000;
        String clientSecret = null;
        String gatewayReference = null;
        String status = "PENDING";

        if (request.getPaymentMethod() == PaymentType.STRIPE) {
            try {
                if (stripeApiKey != null && !stripeApiKey.isBlank()) {
                    long amountInSmallestUnit = Math.round(request.getAmount());
                    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                            .setAmount(amountInSmallestUnit)
                            .setCurrency(request.getCurrency() != null ? request.getCurrency() : "vnd")
                            .setDescription("SkyPOS Order #" + order.getOrderNumber())
                            .putMetadata("orderId", order.getId().toString())
                            .putMetadata("transactionCode", transactionCode)
                            .build();

                    PaymentIntent intent = PaymentIntent.create(params);
                    clientSecret = intent.getClientSecret();
                    gatewayReference = intent.getId();
                } else {
                    // Sandbox simulation
                    gatewayReference = "pi_mock_" + UUID.randomUUID().toString().substring(0, 12);
                    clientSecret = gatewayReference + "_secret_mock";
                }
            } catch (Exception e) {
                log.error("Failed to create Stripe PaymentIntent: {}", e.getMessage(), e);
                throw new RuntimeException("Không thể khởi tạo thanh toán Stripe: " + e.getMessage());
            }
        } else if (request.getPaymentMethod() == PaymentType.CASH) {
            status = "SUCCESS";
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        }

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionCode(transactionCode)
                .order(order)
                .amount(request.getAmount())
                .paymentType(request.getPaymentMethod())
                .status(PaymentStatus.valueOf(status))
                .gatewayReference(gatewayReference)
                .notes("Payment initiated for Order " + order.getOrderNumber())
                .build();

        transaction = transactionRepository.save(transaction);

        return PaymentResponseDTO.builder()
                .transactionId(transaction.getTransactionCode())
                .orderId(order.getId())
                .paymentMethod(request.getPaymentMethod().name())
                .status(status)
                .clientSecret(clientSecret)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public PaymentResponseDTO getTransactionStatus(UUID transactionId) {
        PaymentTransaction transaction = transactionRepository.findById(transactionId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy giao dịch thanh toán")
        );

        return PaymentResponseDTO.builder()
                .transactionId(transaction.getTransactionCode())
                .orderId(transaction.getOrder().getId())
                .paymentMethod(transaction.getPaymentType().name())
                .status(transaction.getStatus().name())
                .amount(transaction.getAmount())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<String> handleStripeWebhook(String payload, String signatureHeader) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventId = root.path("id").asText();
            String eventType = root.path("type").asText();

            log.info("Received Stripe webhook event: id={}, type={}", eventId, eventType);

            // 1. Idempotency Check: prevent duplicate event processing
            if (eventId != null && !processedWebhookEvents.add(eventId)) {
                log.info("Webhook event {} already processed (Idempotent bypass)", eventId);
                return ApiResponse.ok("Event already processed", "IDEMPOTENT");
            }

            JsonNode dataObject = root.path("data").path("object");
            String intentId = dataObject.path("id").asText();

            if ("payment_intent.succeeded".equals(eventType) || "checkout.session.completed".equals(eventType)) {
                handlePaymentSuccess(intentId, dataObject);
            } else if ("payment_intent.payment_failed".equals(eventType)) {
                handlePaymentFailure(intentId, dataObject);
            } else {
                log.info("Unhandled Stripe webhook event type: {}", eventType);
            }

            return ApiResponse.ok("Webhook processed successfully", "SUCCESS");
        } catch (Exception e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Webhook processing error: " + e.getMessage());
        }
    }

    private void handlePaymentSuccess(String intentId, JsonNode dataObject) {
        if (intentId == null || intentId.isBlank()) return;

        PaymentTransaction transaction = transactionRepository.findByGatewayReference(intentId).orElse(null);
        if (transaction != null) {
            transaction.setStatus(PaymentStatus.SUCCESS);
            transaction.setNotes("Payment completed successfully via Stripe Webhook");
            transactionRepository.save(transaction);

            Order order = transaction.getOrder();
            if (order != null) {
                order.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
                log.info("Order {} marked COMPLETED after successful payment {}", order.getId(), intentId);
            }
        } else {
            log.warn("No transaction found matching gatewayReference={}", intentId);
        }
    }

    private void handlePaymentFailure(String intentId, JsonNode dataObject) {
        if (intentId == null || intentId.isBlank()) return;

        PaymentTransaction transaction = transactionRepository.findByGatewayReference(intentId).orElse(null);
        if (transaction != null) {
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setNotes("Payment failed: " + dataObject.path("last_payment_error").path("message").asText("Unknown error"));
            transactionRepository.save(transaction);
            log.warn("Payment transaction {} marked FAILED", transaction.getTransactionCode());
        }
    }
}
