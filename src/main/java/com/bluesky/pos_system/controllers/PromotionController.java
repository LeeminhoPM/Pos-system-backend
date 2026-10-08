package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.ApplyPromotionRequestDTO;
import com.bluesky.pos_system.payload.dto.ApplyPromotionResponseDTO;
import com.bluesky.pos_system.payload.dto.PromotionDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.PromotionService;
import com.bluesky.pos_system.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/promotions", "/api/promotions"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Promotion & Discount Management", description = "APIs for vouchers, coupons, discounts, and real-time promo validation")
public class PromotionController {
    PromotionService promotionService;
    UserService userService;

    @PostMapping
    @Operation(summary = "Create a new promotion voucher")
    public ResponseEntity<PromotionDTO> createPromotion(@Valid @RequestBody PromotionDTO dto) throws Exception {
        User user = userService.getCurrentUser();
        PromotionDTO response = promotionService.createPromotion(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing promotion")
    public ResponseEntity<PromotionDTO> updatePromotion(@PathVariable UUID id, @Valid @RequestBody PromotionDTO dto) {
        PromotionDTO response = promotionService.updatePromotion(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a promotion voucher")
    public ResponseEntity<ApiResponse> deletePromotion(@PathVariable UUID id) {
        promotionService.deletePromotion(id);
        ApiResponse res = new ApiResponse();
        res.setMessage("Xóa mã khuyến mãi thành công");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get promotion details by ID")
    public ResponseEntity<PromotionDTO> getPromotionById(@PathVariable UUID id) {
        PromotionDTO response = promotionService.getPromotionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get all promotions for a store")
    public ResponseEntity<List<PromotionDTO>> getPromotionsByStore(@PathVariable UUID storeId) {
        List<PromotionDTO> response = promotionService.getPromotionsByStore(storeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apply")
    @Operation(summary = "Apply a promotion code to an order and calculate discounted amount")
    public ResponseEntity<ApplyPromotionResponseDTO> applyPromotion(@Valid @RequestBody ApplyPromotionRequestDTO request) {
        ApplyPromotionResponseDTO response = promotionService.applyPromotion(request);
        return ResponseEntity.ok(response);
    }
}
