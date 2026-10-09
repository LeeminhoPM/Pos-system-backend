package com.bluesky.pos_system;

import com.bluesky.pos_system.configuration.StripeProperties;
import com.bluesky.pos_system.domains.OrderStatus;
import com.bluesky.pos_system.domains.PaymentStatus;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Order;
import com.bluesky.pos_system.models.PaymentTransaction;
import com.bluesky.pos_system.payload.dto.*;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.repositories.OrderRepository;
import com.bluesky.pos_system.repositories.PaymentTransactionRepository;
import com.bluesky.pos_system.repositories.RefundRepository;
import com.bluesky.pos_system.services.impl.PaymentServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private StripeProperties stripeProperties;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order testOrder;
    private PaymentTransaction testTransaction;
    private UUID orderId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        Branch branch = Branch.builder()
                .id(UUID.randomUUID())
                .name("Chi nhánh Quận 1")
                .build();

        testOrder = Order.builder()
                .id(orderId)
                .orderNumber("ORD-20261009-001")
                .subtotal(100000.0)
                .totalAmount(100000.0)
                .status(OrderStatus.COMPLETED)
                .branch(branch)
                .build();

        testTransaction = PaymentTransaction.builder()
                .id(transactionId)
                .transactionCode("TXN-TEST-001")
                .order(testOrder)
                .amount(100000.0)
                .currency("vnd")
                .paymentType(PaymentType.STRIPE)
                .status(PaymentStatus.PENDING)
                .gatewayReference("pi_test_123456")
                .refundedAmount(0.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Lấy cấu hình Stripe Gateway cho frontend")
    void testGetStripeConfig() {
        when(stripeProperties.getPublishableKey()).thenReturn("pk_test_sampleKey");
        when(stripeProperties.getCurrency()).thenReturn("vnd");
        when(stripeProperties.isMockMode()).thenReturn(false);

        PaymentConfigResponseDTO config = paymentService.getStripeConfig();

        assertNotNull(config);
        assertEquals("pk_test_sampleKey", config.getPublishableKey());
        assertEquals("vnd", config.getCurrency());
        assertFalse(config.isMockMode());
    }

    @Test
    @DisplayName("Khởi tạo thanh toán CASH thành công -> Order COMPLETED")
    void testInitiatePayment_Cash() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> {
            PaymentTransaction saved = invocation.getArgument(0);
            saved.setId(transactionId);
            return saved;
        });

        PaymentInitiateRequestDTO request = PaymentInitiateRequestDTO.builder()
                .orderId(orderId)
                .amount(100000.0)
                .paymentMethod(PaymentType.CASH)
                .currency("vnd")
                .build();

        PaymentResponseDTO response = paymentService.initiatePayment(request);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(100000.0, response.getAmount());
        assertEquals("CASH", response.getPaymentMethod());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Khởi tạo thanh toán STRIPE trong Sandbox Simulation Mode")
    void testInitiatePayment_Stripe_SimulationMode() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(stripeProperties.isMockMode()).thenReturn(true);
        when(transactionRepository.save(any(PaymentTransaction.class))).thenAnswer(invocation -> {
            PaymentTransaction saved = invocation.getArgument(0);
            saved.setId(transactionId);
            return saved;
        });

        PaymentInitiateRequestDTO request = PaymentInitiateRequestDTO.builder()
                .orderId(orderId)
                .amount(100000.0)
                .paymentMethod(PaymentType.STRIPE)
                .currency("vnd")
                .customerEmail("customer@pos.test")
                .build();

        PaymentResponseDTO response = paymentService.initiatePayment(request);

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertNotNull(response.getClientSecret());
        assertTrue(response.getClientSecret().contains("secret_mock"));
        assertTrue(response.getGatewayReference().startsWith("pi_mock_"));
        verify(transactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    @DisplayName("Khởi tạo thanh toán thất bại nếu không tìm thấy Order")
    void testInitiatePayment_OrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        PaymentInitiateRequestDTO request = PaymentInitiateRequestDTO.builder()
                .orderId(orderId)
                .amount(100000.0)
                .paymentMethod(PaymentType.STRIPE)
                .build();

        assertThrows(EntityNotFoundException.class, () -> paymentService.initiatePayment(request));
    }

    @Test
    @DisplayName("Xử lý Webhook: payment_intent.succeeded -> Cập nhật SUCCESS & lưu thẻ")
    void testHandleStripeWebhook_PaymentIntentSucceeded() {
        when(stripeProperties.getWebhookSecret()).thenReturn(null); // simulation unverified
        when(transactionRepository.findByGatewayReference("pi_test_123456")).thenReturn(Optional.of(testTransaction));

        String fixturePayload = """
        {
          "id": "evt_test_success_1",
          "type": "payment_intent.succeeded",
          "data": {
            "object": {
              "id": "pi_test_123456",
              "metadata": { "transactionCode": "TXN-TEST-001" },
              "latest_charge": "ch_test_charge_999",
              "charges": {
                "data": [
                  {
                    "id": "ch_test_charge_999",
                    "receipt_url": "https://stripe.com/receipt/test",
                    "payment_method_details": {
                      "card": { "brand": "visa", "last4": "4242" }
                    }
                  }
                ]
              }
            }
          }
        }
        """;

        ApiResponse<String> response = paymentService.handleStripeWebhook(fixturePayload, null);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("SUCCESS", response.getData());
        assertEquals(PaymentStatus.SUCCESS, testTransaction.getStatus());
        assertEquals("ch_test_charge_999", testTransaction.getChargeId());
        assertEquals("visa", testTransaction.getCardBrand());
        assertEquals("4242", testTransaction.getCardLast4());
        assertEquals("https://stripe.com/receipt/test", testTransaction.getReceiptUrl());
        verify(transactionRepository, times(1)).save(testTransaction);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Xử lý Webhook Idempotency: Sự kiện trùng lặp không xử lý lại")
    void testHandleStripeWebhook_Idempotency() {
        when(stripeProperties.getWebhookSecret()).thenReturn(null);
        when(transactionRepository.findByGatewayReference("pi_test_123456")).thenReturn(Optional.of(testTransaction));

        String fixturePayload = """
        {
          "id": "evt_duplicate_test_99",
          "type": "payment_intent.succeeded",
          "data": {
            "object": {
              "id": "pi_test_123456",
              "metadata": {}
            }
          }
        }
        """;

        ApiResponse<String> firstCall = paymentService.handleStripeWebhook(fixturePayload, null);
        assertEquals("SUCCESS", firstCall.getData());

        ApiResponse<String> secondCall = paymentService.handleStripeWebhook(fixturePayload, null);
        assertEquals("IDEMPOTENT", secondCall.getData());
    }

    @Test
    @DisplayName("Xử lý Webhook: payment_intent.payment_failed -> Cập nhật FAILED")
    void testHandleStripeWebhook_PaymentIntentFailed() {
        when(stripeProperties.getWebhookSecret()).thenReturn(null);
        when(transactionRepository.findByGatewayReference("pi_test_123456")).thenReturn(Optional.of(testTransaction));

        String fixturePayload = """
        {
          "id": "evt_test_failed_2",
          "type": "payment_intent.payment_failed",
          "data": {
            "object": {
              "id": "pi_test_123456",
              "last_payment_error": { "message": "Card has insufficient funds" },
              "metadata": {}
            }
          }
        }
        """;

        ApiResponse<String> response = paymentService.handleStripeWebhook(fixturePayload, null);

        assertTrue(response.getSuccess());
        assertEquals(PaymentStatus.FAILED, testTransaction.getStatus());
        assertEquals("Card has insufficient funds", testTransaction.getErrorMessage());
        verify(transactionRepository, times(1)).save(testTransaction);
    }

    @Test
    @DisplayName("Xử lý Webhook: charge.failed -> Cập nhật FAILED")
    void testHandleStripeWebhook_ChargeFailed() {
        when(stripeProperties.getWebhookSecret()).thenReturn(null);
        when(transactionRepository.findByGatewayReference("pi_test_123456")).thenReturn(Optional.of(testTransaction));

        String fixturePayload = """
        {
          "id": "evt_charge_failed_3",
          "type": "charge.failed",
          "data": {
            "object": {
              "id": "ch_fail_1",
              "payment_intent": "pi_test_123456",
              "failure_message": "Card was declined"
            }
          }
        }
        """;

        ApiResponse<String> response = paymentService.handleStripeWebhook(fixturePayload, null);

        assertTrue(response.getSuccess());
        assertEquals(PaymentStatus.FAILED, testTransaction.getStatus());
        assertEquals("Card was declined", testTransaction.getErrorMessage());
    }

    @Test
    @DisplayName("Xử lý Webhook: charge.refunded -> Cập nhật REFUNDED")
    void testHandleStripeWebhook_ChargeRefunded() {
        when(stripeProperties.getWebhookSecret()).thenReturn(null);
        when(transactionRepository.findByGatewayReference("pi_test_123456")).thenReturn(Optional.of(testTransaction));

        String fixturePayload = """
        {
          "id": "evt_charge_refunded_4",
          "type": "charge.refunded",
          "data": {
            "object": {
              "id": "ch_refunded_1",
              "payment_intent": "pi_test_123456",
              "amount_refunded": 100000
            }
          }
        }
        """;

        ApiResponse<String> response = paymentService.handleStripeWebhook(fixturePayload, null);

        assertTrue(response.getSuccess());
        assertEquals(PaymentStatus.REFUNDED, testTransaction.getStatus());
        assertEquals(100000.0, testTransaction.getRefundedAmount());
        assertEquals(OrderStatus.REFUNDED, testOrder.getStatus());
    }

    @Test
    @DisplayName("Webhook bảo mật: Ném ngoại lệ khi có webhook secret nhưng thiếu signature header")
    void testHandleStripeWebhook_MissingSignature_WhenSecretConfigured() {
        when(stripeProperties.getWebhookSecret()).thenReturn("whsec_test_secret");

        String payload = "{\"id\":\"evt_1\"}";
        assertThrows(IllegalArgumentException.class, () -> paymentService.handleStripeWebhook(payload, null));
    }

    @Test
    @DisplayName("Hoàn tiền thành công: Hoàn toàn phần (Full Refund)")
    void testRefundPayment_FullRefund() {
        testTransaction.setStatus(PaymentStatus.SUCCESS);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
        when(stripeProperties.isMockMode()).thenReturn(true);

        PaymentRefundRequestDTO request = PaymentRefundRequestDTO.builder()
                .transactionId(transactionId)
                .amount(null) // Hoàn 100%
                .reason("requested_by_customer")
                .build();

        PaymentRefundResponseDTO response = paymentService.refundPayment(request);

        assertNotNull(response);
        assertEquals("REFUNDED", response.getStatus());
        assertEquals(100000.0, response.getRefundedAmount());
        assertEquals(100000.0, response.getTotalRefunded());
        assertEquals(OrderStatus.REFUNDED, testOrder.getStatus());
        verify(transactionRepository, times(1)).save(testTransaction);
        verify(refundRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Hoàn tiền thành công: Hoàn một phần (Partial Refund)")
    void testRefundPayment_PartialRefund() {
        testTransaction.setStatus(PaymentStatus.SUCCESS);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
        when(stripeProperties.isMockMode()).thenReturn(true);

        PaymentRefundRequestDTO request = PaymentRefundRequestDTO.builder()
                .transactionId(transactionId)
                .amount(40000.0) // Hoàn một phần
                .reason("duplicate")
                .build();

        PaymentRefundResponseDTO response = paymentService.refundPayment(request);

        assertNotNull(response);
        assertEquals("PARTIALLY_REFUNDED", response.getStatus());
        assertEquals(40000.0, response.getRefundedAmount());
        assertEquals(40000.0, response.getTotalRefunded());
        assertEquals(OrderStatus.COMPLETED, testOrder.getStatus()); // Order giữ nguyên nếu hoàn 1 phần
    }

    @Test
    @DisplayName("Hoàn tiền thất bại nếu số tiền hoàn vượt quá số tiền giao dịch")
    void testRefundPayment_ExceedsBalance() {
        testTransaction.setStatus(PaymentStatus.SUCCESS);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        PaymentRefundRequestDTO request = PaymentRefundRequestDTO.builder()
                .transactionId(transactionId)
                .amount(150000.0) // Vượt quá 100000.0
                .build();

        assertThrows(IllegalArgumentException.class, () -> paymentService.refundPayment(request));
    }

    @Test
    @DisplayName("Hoàn tiền thất bại nếu trạng thái giao dịch chưa SUCCESS")
    void testRefundPayment_InvalidStatus() {
        testTransaction.setStatus(PaymentStatus.PENDING);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        PaymentRefundRequestDTO request = PaymentRefundRequestDTO.builder()
                .transactionId(transactionId)
                .build();

        assertThrows(IllegalStateException.class, () -> paymentService.refundPayment(request));
    }

    @Test
    @DisplayName("Lấy lịch sử giao dịch thanh toán phân trang")
    void testGetPaymentHistory() {
        Page<PaymentTransaction> page = new PageImpl<>(List.of(testTransaction));
        when(transactionRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<PaymentResponseDTO> result = paymentService.getPaymentHistory(
                null, null, null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("TXN-TEST-001", result.getContent().get(0).getTransactionId());
    }

    @Test
    @DisplayName("Lấy danh sách giao dịch theo mã đơn hàng")
    void testGetTransactionsByOrder() {
        when(transactionRepository.findByOrderIdOrderByCreatedAtDesc(orderId)).thenReturn(List.of(testTransaction));

        List<PaymentResponseDTO> list = paymentService.getTransactionsByOrder(orderId);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("pi_test_123456", list.get(0).getGatewayReference());
    }
}
