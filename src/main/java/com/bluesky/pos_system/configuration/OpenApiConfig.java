package com.bluesky.pos_system.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("SkyPOS Enterprise RESTful API")
                        .version("v1.0.0")
                        .description("Tài liệu chính thức cho Hệ thống Bán lẻ & Điểm bán hàng SkyPOS (Point of Sale).\n\n" +
                                "Các tính năng chính:\n" +
                                "- Quản lý Bán hàng & Điểm bán (POS Checkout, Quét mã vạch)\n" +
                                "- Quản trị Sản phẩm, Danh mục, Tồn kho & Nhà cung cấp\n" +
                                "- Xử lý Đơn hàng, Hoàn trả & Khuyến mãi (Voucher / Coupon)\n" +
                                "- Cổng thanh toán trực tuyến Stripe & Webhook Idempotency\n" +
                                "- Phân quyền nhân viên (Store Admin, Branch Manager, Cashier)\n" +
                                "- Thống kê doanh thu & Báo cáo ca làm việc (Shift Report)")
                        .contact(new Contact()
                                .name("SkyPOS Engineering Team")
                                .email("dev@skypos.vn")
                                .url("https://skypos.vn"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:5000").description("Môi trường Local Development"),
                        new Server().url("https://api.skypos.vn").description("Môi trường Production (HTTPS)")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT Access Token để xác thực (Ví dụ: Bearer eyJhbGci...)")
                        )
                );
    }
}
