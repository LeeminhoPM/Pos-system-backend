package com.bluesky.pos_system.payload.dto;

import com.bluesky.pos_system.domains.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Tham số tìm kiếm, phân trang và sắp xếp sản phẩm")
public class ProductSearchFilterDTO {

    @Schema(description = "Từ khóa tìm kiếm (Tên, Mã SKU, Barcode, Thương hiệu)", example = "Cà phê")
    String search;

    @Schema(description = "Lọc theo danh mục sản phẩm (UUID)")
    UUID categoryId;

    @Schema(description = "Lọc theo trạng thái tồn kho: IN_STOCK, OUT_OF_STOCK, LOW_STOCK")
    ProductStatus status;

    @Schema(description = "Khoảng giá bán tối thiểu")
    Double minPrice;

    @Schema(description = "Khoảng giá bán tối đa")
    Double maxPrice;

    @Schema(description = "Số thứ tự trang (bắt đầu từ 0)", example = "0")
    @Builder.Default
    Integer page = 0;

    @Schema(description = "Số lượng bản ghi trên một trang", example = "10")
    @Builder.Default
    Integer size = 10;

    @Schema(description = "Trường sắp xếp (name, sellingPrice, createdAt, etc.)", example = "name")
    @Builder.Default
    String sortBy = "name";

    @Schema(description = "Hướng sắp xếp: asc hoặc desc", example = "asc")
    @Builder.Default
    String sortDir = "asc";
}
