package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.payload.dto.CategoryDTO;
import com.bluesky.pos_system.payload.dto.CreateOnlineOrderRequestDTO;
import com.bluesky.pos_system.payload.dto.DeliveryOrderDTO;
import com.bluesky.pos_system.payload.dto.PublicProductDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.DeliveryOrderService;
import com.bluesky.pos_system.services.PublicCatalogService;
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
@RequestMapping({"/api/v1/public", "/api/public"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Public Web Shop & Catalog", description = "Các API công khai dành cho khách hàng vãng lai duyệt thực đơn, danh mục và đặt hàng trực tuyến")
public class PublicCatalogController {
    PublicCatalogService publicCatalogService;
    DeliveryOrderService deliveryOrderService;

    @GetMapping("/catalog")
    @Operation(summary = "Xem catalog sản phẩm công khai", description = "Lấy danh sách sản phẩm đang bán, có bộ lọc theo danh mục, từ khóa tìm kiếm (Cache Redis tối ưu)")
    public ResponseEntity<ApiResponse<List<PublicProductDTO>>> getPublicCatalog(
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String keyword) {
        List<PublicProductDTO> response = publicCatalogService.getPublicCatalog(storeId, categoryId, keyword);
        return ResponseEntity.ok(ApiResponse.ok(response, "Danh mục sản phẩm"));
    }

    @GetMapping("/catalog/{productId}")
    @Operation(summary = "Xem chi tiết sản phẩm công khai")
    public ResponseEntity<ApiResponse<PublicProductDTO>> getPublicProductDetail(@PathVariable UUID productId) {
        PublicProductDTO response = publicCatalogService.getPublicProductDetail(productId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Chi tiết sản phẩm"));
    }

    @GetMapping("/categories")
    @Operation(summary = "Xem danh mục sản phẩm công khai")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getPublicCategories(@RequestParam(required = false) UUID storeId) {
        List<CategoryDTO> response = publicCatalogService.getPublicCategories(storeId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Danh sách ngành hàng"));
    }

    @PostMapping("/orders/checkout")
    @Operation(summary = "Đặt hàng online giao tận nơi", description = "Khách hàng tự đặt đơn trực tuyến, chọn địa chỉ và phương thức thanh toán")
    public ResponseEntity<ApiResponse<DeliveryOrderDTO>> placeOnlineOrder(@Valid @RequestBody CreateOnlineOrderRequestDTO request) {
        DeliveryOrderDTO response = deliveryOrderService.createOnlineDeliveryOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Đặt hàng thành công"));
    }

    @GetMapping("/delivery/track/{trackingNumber}")
    @Operation(summary = "Tra cứu tiến độ giao hàng bằng mã vận đơn")
    public ResponseEntity<ApiResponse<DeliveryOrderDTO>> trackDelivery(@PathVariable String trackingNumber) {
        DeliveryOrderDTO response = deliveryOrderService.trackDelivery(trackingNumber);
        return ResponseEntity.ok(ApiResponse.ok(response, "Thông tin vận chuyển"));
    }
}
