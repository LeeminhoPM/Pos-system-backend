package com.bluesky.pos_system;

import com.bluesky.pos_system.controllers.PaymentController;
import com.bluesky.pos_system.domains.PaymentType;
import com.bluesky.pos_system.payload.dto.*;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("GET /api/v1/payments/config -> Trả về Publishable Key")
    void testGetStripeConfig() throws Exception {
        PaymentConfigResponseDTO config = PaymentConfigResponseDTO.builder()
                .publishableKey("pk_test_sample")
                .currency("vnd")
                .mockMode(false)
                .build();

        when(paymentService.getStripeConfig()).thenReturn(config);

        mockMvc.perform(get("/api/v1/payments/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publishableKey").value("pk_test_sample"))
                .andExpect(jsonPath("$.data.currency").value("vnd"))
                .andExpect(jsonPath("$.data.mockMode").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/payments/initiate -> Tạo Payment Intent")
    void testInitiatePayment() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentInitiateRequestDTO request = PaymentInitiateRequestDTO.builder()
                .orderId(orderId)
                .amount(120000.0)
                .paymentMethod(PaymentType.STRIPE)
                .currency("vnd")
                .build();

        PaymentResponseDTO response = PaymentResponseDTO.builder()
                .transactionId("TXN-123456")
                .orderId(orderId)
                .paymentMethod("STRIPE")
                .status("PENDING")
                .clientSecret("pi_test_secret")
                .amount(120000.0)
                .build();

        when(paymentService.initiatePayment(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN-123456"))
                .andExpect(jsonPath("$.clientSecret").value("pi_test_secret"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/refund -> Hoàn tiền thành công")
    void testRefundPayment() throws Exception {
        UUID txnId = UUID.randomUUID();
        PaymentRefundRequestDTO request = PaymentRefundRequestDTO.builder()
                .transactionId(txnId)
                .amount(50000.0)
                .reason("requested_by_customer")
                .build();

        PaymentRefundResponseDTO response = PaymentRefundResponseDTO.builder()
                .refundId("re_test_999")
                .transactionId(txnId)
                .transactionCode("TXN-123456")
                .refundedAmount(50000.0)
                .totalRefunded(50000.0)
                .status("PARTIALLY_REFUNDED")
                .reason("requested_by_customer")
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentService.refundPayment(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.refundId").value("re_test_999"))
                .andExpect(jsonPath("$.data.status").value("PARTIALLY_REFUNDED"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/history -> Lấy lịch sử giao dịch")
    void testGetPaymentHistory() throws Exception {
        PaymentResponseDTO item = PaymentResponseDTO.builder()
                .transactionId("TXN-123")
                .amount(200000.0)
                .status("SUCCESS")
                .build();

        when(paymentService.getPaymentHistory(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(item)));

        mockMvc.perform(get("/api/v1/payments/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].transactionId").value("TXN-123"))
                .andExpect(jsonPath("$.data.content[0].status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/webhook/stripe -> Webhook thành công")
    void testHandleWebhookSuccess() throws Exception {
        String payload = "{\"id\":\"evt_123\",\"type\":\"payment_intent.succeeded\"}";
        when(paymentService.handleStripeWebhook(any(), any()))
                .thenReturn(ApiResponse.ok("SUCCESS", "Webhook processed successfully"));

        mockMvc.perform(post("/api/v1/payments/webhook/stripe")
                        .header("Stripe-Signature", "t=123,v1=abc")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/payments/webhook/stripe -> Chữ ký không hợp lệ trả về HTTP 400")
    void testHandleWebhookInvalidSignature() throws Exception {
        String payload = "{\"id\":\"evt_123\"}";
        when(paymentService.handleStripeWebhook(any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid signature"));

        mockMvc.perform(post("/api/v1/payments/webhook/stripe")
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid signature"));
    }
}
