package com.bluesky.pos_system.services.impl;

import com.bluesky.pos_system.configuration.StripeProperties;
import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.models.PaymentTransaction;
import com.bluesky.pos_system.models.Refund;
import com.bluesky.pos_system.payload.dto.*;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.repositories.PaymentTransactionRepository;
import com.bluesky.pos_system.repositories.RefundRepository;
import com.bluesky.pos_system.services.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentServiceImpl implements PaymentService {

    PaymentTransactionRepository transactionRepository;
    OrderRepository orderRepository;
    RefundRepository refundRepository;
    StripeProperties stripeProperties;
    ObjectMapper objectMapper = new ObjectMapper();

    // Idempotency cache for processed webhook event IDs
    Set<String> processedWebhookEvents = ConcurrentHashMap.newKeySet();

    private static final Set<String> ZERO_DECIMAL_CURRENCIES = Set.of(
            "bif", "clp", "djf", "gnf", "jpy", "kmf", "krw", "mga", "pyg",
            "rwf", "ugx", "vnd", "vuv", "xaf", "xof", "xpf"
    );

    @Override
    public PaymentConfigResponseDTO getStripeConfig() {
        return PaymentConfigResponseDTO.builder()
                .publishableKey(stripeProperties.getPublishableKey())
                .currency(stripeProperties.getCurrency() != null ? stripeProperties.getCurrency() : "vnd")
                .mockMode(stripeProperties.isMockMode())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponseDTO initiatePayment(PaymentInitiateRequestDTO request) {
        Order order = orderRepository.findById(request.getOrderId()).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + request.getOrderId())
        );

        String transactionCode = "TXN-" + (System.currentTimeMillis() % 10000000) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String clientSecret = null;
        String gatewayReference = null;
        PaymentStatus status = PaymentStatus.PENDING;
        String currency = (request.getCurrency() != null && !request.getCurrency().isBlank())
                ? request.getCurrency().toLowerCase()
                : (stripeProperties.getCurrency() != null ? stripeProperties.getCurrency().toLowerCase() : "vnd");

        if (request.getPaymentMethod() == PaymentType.STRIPE) {
            try {
                if (!stripeProperties.isMockMode()) {
                    long amountInSmallestUnit = calculateSmallestUnit(request.getAmount(), currency);

                    RequestOptions requestOptions = RequestOptions.builder()
                            .setIdempotencyKey(transactionCode)
                            .build();

                    PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                            .setAmount(amountInSmallestUnit)
                            .setCurrency(currency)
                            .setDescription("SkyPOS Order #" + order.getOrderNumber())
                            .putMetadata("orderId", order.getId().toString())
                            .putMetadata("orderNumber", order.getOrderNumber())
                            .putMetadata("transactionCode", transactionCode)
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                            );

                    if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
                        paramsBuilder.setReceiptEmail(request.getCustomerEmail());
                    }

                    PaymentIntent intent = PaymentIntent.create(paramsBuilder.build(), requestOptions);
                    clientSecret = intent.getClientSecret();
                    gatewayReference = intent.getId();
                    log.info("Created Stripe PaymentIntent: id={}, amount={}, currency={}", intent.getId(), amountInSmallestUnit, currency);
                } else {
                    // Sandbox simulation mode
                    gatewayReference = "pi_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                    clientSecret = gatewayReference + "_secret_mock";
                    log.info("Sandbox simulation mode: Created mock PaymentIntent id={}", gatewayReference);
                }
            } catch (Exception e) {
                log.error("Failed to create Stripe PaymentIntent: {}", e.getMessage(), e);
                throw new RuntimeException("Không thể khởi tạo thanh toán Stripe: " + e.getMessage(), e);
            }
        } else if (request.getPaymentMethod() == PaymentType.CASH) {
            status = PaymentStatus.SUCCESS;
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        }

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionCode(transactionCode)
                .order(order)
                .amount(request.getAmount())
                .currency(currency)
                .paymentType(request.getPaymentMethod())
                .status(status)
                .gatewayReference(gatewayReference)
                .customerEmail(request.getCustomerEmail())
                .refundedAmount(0.0)
                .notes("Payment initiated for Order " + order.getOrderNumber())
                .build();

        transaction = transactionRepository.save(transaction);

        PaymentResponseDTO response = mapToResponseDTO(transaction);
        response.setClientSecret(clientSecret);
        return response;
    }

    @Override
    public PaymentResponseDTO getTransactionStatus(UUID transactionId) {
        PaymentTransaction transaction = transactionRepository.findById(transactionId).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy giao dịch thanh toán với ID: " + transactionId)
        );
        return mapToResponseDTO(transaction);
    }

    @Override
    @Transactional
    public ApiResponse<String> handleStripeWebhook(String payload, String signatureHeader) {
        try {
            // Verify Webhook Signature if secret is configured
            if (stripeProperties.getWebhookSecret() != null && !stripeProperties.getWebhookSecret().isBlank()) {
                if (signatureHeader == null || signatureHeader.isBlank()) {
                    log.error("Missing Stripe-Signature header while webhook secret is configured");
                    throw new IllegalArgumentException("Thiếu Stripe-Signature header khi xác thực webhook");
                }
                try {
                    Webhook.constructEvent(payload, signatureHeader, stripeProperties.getWebhookSecret());
                    log.debug("Stripe webhook signature verified successfully");
                } catch (SignatureVerificationException e) {
                    log.error("Stripe webhook signature verification failed: {}", e.getMessage());
                    throw new IllegalArgumentException("Chữ ký Stripe Webhook không hợp lệ: " + e.getMessage());
                }
            } else {
                log.warn("Stripe webhook secret is not configured. Processing webhook in development/unverified mode.");
            }

            JsonNode root = objectMapper.readTree(payload);
            String eventId = root.path("id").asText();
            String eventType = root.path("type").asText();

            log.info("Processing Stripe webhook event: id={}, type={}", eventId, eventType);

            // Webhook Idempotency Check
            if (eventId != null && !processedWebhookEvents.add(eventId)) {
                log.info("Webhook event {} already processed (Idempotent bypass)", eventId);
                return ApiResponse.ok("IDEMPOTENT", "Sự kiện webhook đã được xử lý trước đó");
            }

            JsonNode dataObject = root.path("data").path("object");

            switch (eventType) {
                case "payment_intent.succeeded":
                case "checkout.session.completed":
                    handlePaymentSuccess(dataObject);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailure(dataObject);
                    break;
                case "charge.failed":
                    handleChargeFailure(dataObject);
                    break;
                case "charge.refunded":
                    handleChargeRefunded(dataObject);
                    break;
                default:
                    log.info("Unhandled Stripe webhook event type: {}", eventType);
                    break;
            }

            return ApiResponse.ok("SUCCESS", "Xử lý Stripe Webhook thành công");
        } catch (IllegalArgumentException e) {
            log.error("Validation error in Stripe webhook: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi xử lý Stripe webhook: " + e.getMessage(), e);
        }
    }

    private void handlePaymentSuccess(JsonNode dataObject) {
        String intentId = dataObject.path("id").asText();
        String transactionCode = dataObject.path("metadata").path("transactionCode").asText(null);

        PaymentTransaction transaction = findTransaction(intentId, transactionCode);
        if (transaction != null) {
            if (transaction.getStatus() == PaymentStatus.SUCCESS) {
                log.info("Transaction {} already marked SUCCESS (Idempotent)", transaction.getTransactionCode());
                return;
            }

            transaction.setStatus(PaymentStatus.SUCCESS);
            transaction.setNotes("Payment completed successfully via Stripe Webhook");

            // Extract card & charge details if available
            JsonNode latestCharge = dataObject.path("latest_charge");
            if (latestCharge.isTextual()) {
                transaction.setChargeId(latestCharge.asText());
            }

            JsonNode chargesList = dataObject.path("charges").path("data");
            if (chargesList.isArray() && !chargesList.isEmpty()) {
                JsonNode chargeObj = chargesList.get(0);
                if (transaction.getChargeId() == null) {
                    transaction.setChargeId(chargeObj.path("id").asText(null));
                }
                transaction.setReceiptUrl(chargeObj.path("receipt_url").asText(null));

                JsonNode cardNode = chargeObj.path("payment_method_details").path("card");
                if (!cardNode.isMissingNode()) {
                    transaction.setCardBrand(cardNode.path("brand").asText(null));
                    transaction.setCardLast4(cardNode.path("last4").asText(null));
                }
            }

            transactionRepository.save(transaction);

            Order order = transaction.getOrder();
            if (order != null) {
                order.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
                log.info("Order {} marked COMPLETED after successful Stripe payment {}", order.getId(), intentId);
            }
        } else {
            log.warn("No transaction found matching intentId={} or transactionCode={}", intentId, transactionCode);
        }
    }

    private void handlePaymentIntentFailure(JsonNode dataObject) {
        String intentId = dataObject.path("id").asText();
        String transactionCode = dataObject.path("metadata").path("transactionCode").asText(null);

        PaymentTransaction transaction = findTransaction(intentId, transactionCode);
        if (transaction != null) {
            String errorMsg = dataObject.path("last_payment_error").path("message").asText("Payment failed");
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setErrorMessage(errorMsg);
            transaction.setNotes("Thanh toán Stripe thất bại: " + errorMsg);
            transactionRepository.save(transaction);
            log.warn("Payment transaction {} marked FAILED: {}", transaction.getTransactionCode(), errorMsg);
        }
    }

    private void handleChargeFailure(JsonNode dataObject) {
        String paymentIntentId = dataObject.path("payment_intent").asText(null);
        String chargeId = dataObject.path("id").asText(null);

        PaymentTransaction transaction = null;
        if (paymentIntentId != null && !paymentIntentId.isBlank()) {
            transaction = transactionRepository.findByGatewayReference(paymentIntentId).orElse(null);
        }

        if (transaction != null) {
            String failureMessage = dataObject.path("failure_message").asText("Charge failed");
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setChargeId(chargeId);
            transaction.setErrorMessage(failureMessage);
            transaction.setNotes("Charge thất bại: " + failureMessage);
            transactionRepository.save(transaction);
            log.warn("Transaction {} updated with charge.failed: {}", transaction.getTransactionCode(), failureMessage);
        }
    }

    private void handleChargeRefunded(JsonNode dataObject) {
        String paymentIntentId = dataObject.path("payment_intent").asText(null);
        PaymentTransaction transaction = null;
        if (paymentIntentId != null && !paymentIntentId.isBlank()) {
            transaction = transactionRepository.findByGatewayReference(paymentIntentId).orElse(null);
        }

        if (transaction != null) {
            long amountRefundedRaw = dataObject.path("amount_refunded").asLong(0);
            double refundedAmount = convertFromSmallestUnit(amountRefundedRaw, transaction.getCurrency());
            transaction.setRefundedAmount(refundedAmount);
            if (refundedAmount >= transaction.getAmount()) {
                transaction.setStatus(PaymentStatus.REFUNDED);
                if (transaction.getOrder() != null) {
                    transaction.getOrder().setStatus(OrderStatus.REFUNDED);
                    orderRepository.save(transaction.getOrder());
                }
            } else {
                transaction.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }
            transaction.setNotes("Giao dịch được hoàn tiền qua Stripe Webhook: " + refundedAmount);
            transactionRepository.save(transaction);
            log.info("Transaction {} updated with charge.refunded: amount={}", transaction.getTransactionCode(), refundedAmount);
        }
    }

    private PaymentTransaction findTransaction(String gatewayReference, String transactionCode) {
        if (gatewayReference != null && !gatewayReference.isBlank()) {
            Optional<PaymentTransaction> byGateway = transactionRepository.findByGatewayReference(gatewayReference);
            if (byGateway.isPresent()) return byGateway.get();
        }
        if (transactionCode != null && !transactionCode.isBlank()) {
            return transactionRepository.findByTransactionCode(transactionCode).orElse(null);
        }
        return null;
    }

    @Override
    @Transactional
    public PaymentRefundResponseDTO refundPayment(PaymentRefundRequestDTO request) {
        PaymentTransaction transaction = transactionRepository.findById(request.getTransactionId()).orElseThrow(
                () -> new EntityNotFoundException("Không tìm thấy giao dịch thanh toán với ID: " + request.getTransactionId())
        );

        if (transaction.getStatus() != PaymentStatus.SUCCESS && transaction.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new IllegalStateException("Chỉ có thể hoàn tiền cho giao dịch đã thanh toán thành công (Trạng thái hiện tại: " + transaction.getStatus() + ")");
        }

        double alreadyRefunded = transaction.getRefundedAmount() != null ? transaction.getRefundedAmount() : 0.0;
        double remainingRefundable = transaction.getAmount() - alreadyRefunded;

        double refundAmount = (request.getAmount() == null) ? remainingRefundable : request.getAmount();

        if (refundAmount <= 0 || refundAmount > remainingRefundable) {
            throw new IllegalArgumentException(String.format(
                    "Số tiền hoàn không hợp lệ (%.2f). Số tiền tối đa có thể hoàn là: %.2f %s",
                    refundAmount, remainingRefundable, transaction.getCurrency()
            ));
        }

        String stripeRefundId = null;
        String currency = transaction.getCurrency() != null ? transaction.getCurrency().toLowerCase() : "vnd";

        // Call Stripe Refund API if not in mock mode and reference is a valid Stripe PaymentIntent
        if (!stripeProperties.isMockMode() && transaction.getGatewayReference() != null && transaction.getGatewayReference().startsWith("pi_")) {
            try {
                long amountInSmallestUnit = calculateSmallestUnit(refundAmount, currency);
                RefundCreateParams.Builder refundParams = RefundCreateParams.builder()
                        .setPaymentIntent(transaction.getGatewayReference())
                        .setAmount(amountInSmallestUnit);

                if (request.getReason() != null && !request.getReason().isBlank()) {
                    try {
                        refundParams.setReason(RefundCreateParams.Reason.valueOf(request.getReason().toUpperCase()));
                    } catch (Exception ex) {
                        refundParams.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
                    }
                }

                com.stripe.model.Refund stripeRefund = com.stripe.model.Refund.create(refundParams.build());
                stripeRefundId = stripeRefund.getId();
                log.info("Stripe refund created successfully: id={}, amount={}", stripeRefundId, amountInSmallestUnit);
            } catch (Exception e) {
                log.error("Stripe refund failed: {}", e.getMessage(), e);
                throw new RuntimeException("Không thể xử lý hoàn tiền qua cổng Stripe: " + e.getMessage(), e);
            }
        } else {
            stripeRefundId = "re_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            log.info("Sandbox simulation mode: Created mock refund id={}", stripeRefundId);
        }

        double totalRefunded = alreadyRefunded + refundAmount;
        transaction.setRefundedAmount(totalRefunded);
        transaction.setStripeRefundId(stripeRefundId);

        boolean isFullRefund = totalRefunded >= transaction.getAmount();
        transaction.setStatus(isFullRefund ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);
        transaction.setNotes("Hoàn tiền: " + refundAmount + " " + currency + " (Refund ID: " + stripeRefundId + ")");
        transactionRepository.save(transaction);

        Order order = transaction.getOrder();
        if (order != null && isFullRefund) {
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);
        }

        // Record in Refund repository to sync with POS Shift Reports
        try {
            Refund refundRecord = Refund.builder()
                    .order(order)
                    .amount(refundAmount)
                    .reason(request.getReason())
                    .branch(order != null ? order.getBranch() : null)
                    .cashier(order != null ? order.getCashier() : null)
                    .paymentType(transaction.getPaymentType())
                    .build();
            refundRepository.save(refundRecord);
        } catch (Exception e) {
            log.warn("Could not save POS refund record: {}", e.getMessage());
        }

        return PaymentRefundResponseDTO.builder()
                .refundId(stripeRefundId)
                .transactionId(transaction.getId())
                .transactionCode(transaction.getTransactionCode())
                .orderId(order != null ? order.getId() : null)
                .orderNumber(order != null ? order.getOrderNumber() : null)
                .refundedAmount(refundAmount)
                .totalRefunded(totalRefunded)
                .status(transaction.getStatus().name())
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public Page<PaymentResponseDTO> getPaymentHistory(
            UUID branchId, UUID orderId, PaymentStatus status, PaymentType paymentType, Pageable pageable) {

        Specification<PaymentTransaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (branchId != null) {
                predicates.add(cb.equal(root.get("order").get("branch").get("id"), branchId));
            }
            if (orderId != null) {
                predicates.add(cb.equal(root.get("order").get("id"), orderId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (paymentType != null) {
                predicates.add(cb.equal(root.get("paymentType"), paymentType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return transactionRepository.findAll(spec, pageable).map(this::mapToResponseDTO);
    }

    @Override
    public List<PaymentResponseDTO> getTransactionsByOrder(UUID orderId) {
        return transactionRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private PaymentResponseDTO mapToResponseDTO(PaymentTransaction transaction) {
        Order order = transaction.getOrder();
        return PaymentResponseDTO.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionCode())
                .orderId(order != null ? order.getId() : null)
                .orderNumber(order != null ? order.getOrderNumber() : null)
                .paymentMethod(transaction.getPaymentType() != null ? transaction.getPaymentType().name() : null)
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .gatewayReference(transaction.getGatewayReference())
                .chargeId(transaction.getChargeId())
                .receiptUrl(transaction.getReceiptUrl())
                .refundedAmount(transaction.getRefundedAmount() != null ? transaction.getRefundedAmount() : 0.0)
                .cardBrand(transaction.getCardBrand())
                .cardLast4(transaction.getCardLast4())
                .customerEmail(transaction.getCustomerEmail())
                .notes(transaction.getNotes())
                .errorMessage(transaction.getErrorMessage())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    private long calculateSmallestUnit(double amount, String currency) {
        if (ZERO_DECIMAL_CURRENCIES.contains(currency.toLowerCase())) {
            return Math.round(amount);
        }
        return Math.round(amount * 100);
    }

    private double convertFromSmallestUnit(long rawAmount, String currency) {
        if (currency != null && ZERO_DECIMAL_CURRENCIES.contains(currency.toLowerCase())) {
            return (double) rawAmount;
        }
        return rawAmount / 100.0;
    }
}
