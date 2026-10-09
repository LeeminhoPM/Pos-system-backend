# 🚀 SkyPOS Enterprise - Hệ Thống Quản Lý Bán Hàng & Kho Đa Chi Nhánh

[![CI/CD Pipeline](https://github.com/LeeminhoPM/Pos-system-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/LeeminhoPM/Pos-system-backend/actions/workflows/ci.yml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-orange.svg)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-19-blue.svg)](https://react.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![Stripe](https://img.shields.io/badge/Stripe-Enabled-635bff.svg)](https://stripe.com/)

**SkyPOS Enterprise** là nền tảng quản lý điểm bán lẻ (Point of Sale), kho vận đa chi nhánh và thanh toán trực tuyến hiện đại. Hệ thống được xây dựng theo kiến trúc Micro-ready Monolith kết hợp giữa **Spring Boot 3** (Java 17/21) và **React 19 (Vite + Tailwind CSS)**, đáp ứng các tiêu chuẩn khắt khe về hiệu năng cao, bảo mật tài chính và sẵn sàng mở rộng cho môi trường Production.

---

## 📑 Mục Lục
- [1. Tính Năng Nổi Bật](#1-tính-năng-nổi-bật)
- [2. Công Nghệ Sử Dụng (Tech Stack)](#2-công-nghệ-sử-dụng-tech-stack)
- [3. Cấu Trúc Thư Mục Dự Án](#3-cấu-trúc-thư-mục-dự-án)
- [4. Yêu Cầu Môi Trường (Prerequisites)](#4-yêu-cầu-môi-trường-prerequisites)
- [5. Hướng Dẫn Chạy Môi Trường Local](#5-hướng-dẫn-chạy-môi-trường-local)
- [6. Hướng Dẫn Chạy Bằng Docker & Docker Compose](#6-hướng-dẫn-chạy-bằng-docker--docker-compose)
- [7. Biến Môi Trường (Environment Variables)](#7-biến-môi-trường-environment-variables)
- [8. API Reference & Danh Sách Endpoints Cốt Lõi](#8-api-reference--danh-sách-endpoints-cốt-lõi)
- [9. Hướng Dẫn Kiểm Thử & Đảm Bảo Chất Lượng Code](#9-hướng-dẫn-kiểm-thử--đảm-bảo-chất-lượng-code)
- [10. CI/CD & Hướng Dẫn Triển Khai Production](#10-cicd--hướng-dẫn-triển-khai-production)

---

## 1. Tính Năng Nổi Bật

- 🛒 **Giao diện POS bán hàng siêu tốc**: Tìm kiếm nhanh mã vạch Barcode/SKU, chọn chiết khấu, áp dụng voucher khuyến mãi, quét thẻ và in hóa đơn.
- 💳 **Cổng thanh toán quốc tế Stripe**: Hỗ trợ thẻ tín dụng/ghi nợ quốc tế (Visa, Mastercard), tự động đồng bộ Webhook, giả lập Sandbox và hoàn tiền (Refunds) an toàn.
- 🏢 **Kiến trúc Đa chi nhánh & Cửa hàng (Multi-Store & Multi-Branch)**: Mỗi tài khoản được phân quyền chặt chẽ theo chi nhánh và cửa hàng.
- 📦 **Quản lý kho thời gian thực (Real-time Inventory Audit)**: Cảnh báo hàng sắp hết (Low-stock warning), sổ cái nhật ký xuất/nhập/điều chỉnh kho (`InventoryTransaction`).
- 🕒 **Quản lý ca làm việc thu ngân (Shift Reports)**: Khai báo tiền đầu ca, theo dõi doanh thu tiền mặt / thẻ trong ca, chốt ca và xuất báo cáo bàn giao.
- ⚡ **Tối ưu hóa hiệu năng & Cache**: Sử dụng **Redis Cache** cho danh mục sản phẩm, cấu hình JPA `@EntityGraph` chống triệt để lỗi N+1 Query và `LazyInitializationException`.
- 🔐 **Bảo mật chuẩn Enterprise**: Spring Security 6 stateless với JWT Token (HMAC-SHA512), mã hóa mật khẩu BCrypt, phòng chống tấn công CSRF/XSS, phân quyền RBAC (`ADMIN`, `STORE_MANAGER`, `CASHIER`).

---

## 2. Công Nghệ Sử Dụng (Tech Stack)

### Backend
- **Core Framework**: Spring Boot 3.5.x, Java 17 / 21
- **Persistence & ORM**: Spring Data JPA, Hibernate ORM, HikariCP
- **Database**: MySQL 8.0+
- **Database Migration**: Flyway 10+
- **Cache Engine**: Redis 7+ (`spring-data-redis`)
- **Security**: Spring Security, JJWT (io.jsonwebtoken 0.12.6)
- **Payment Gateway**: Stripe Java SDK 31.1.0
- **Documentation**: SpringDoc OpenAPI / Swagger UI 3.0
- **Monitoring**: Spring Boot Actuator, Micrometer Prometheus Metrics

### Frontend (`pos-ui`)
- **Core**: React 19, Vite 8, JavaScript (ESNext)
- **Styling**: Tailwind CSS v4, Lucide React Icons
- **State & Forms**: Zustand, React Hook Form, Zod Validation
- **Network**: Axios với Interceptors chuẩn hóa lỗi (`ApiError`)
- **Payments UI**: `@stripe/react-stripe-js`, `@stripe/stripe-js`

### DevOps & CI/CD
- **Containerization**: Docker Multi-stage Builds, Docker Compose
- **Web Server / Reverse Proxy**: Nginx Alpine
- **CI/CD**: GitHub Actions (Compile, Checkstyle, JaCoCo Coverage, Test, Docker Build)
- **Code Quality**: Maven Checkstyle Plugin, ESLint 9

---

## 3. Cấu Trúc Thư Mục Dự Án

```
pos-system/
├── .github/
│   └── workflows/
│       └── ci.yml                     # Pipeline GitHub Actions CI/CD
├── docs/
│   └── DATABASE_SCHEMA.md             # Tài liệu mô tả Schema DB & ERD
├── pos-ui/                            # Ứng dụng Frontend React Single Page App
│   ├── src/
│   │   ├── components/                # UI Components (Modals, Payment, Layout)
│   │   ├── features/                  # Modules chức năng (POS, Orders, Inventory)
│   │   ├── services/                  # Axios apiClient & API Endpoints
│   │   ├── config/                    # Cấu hình biến môi trường Frontend
│   │   └── App.jsx
│   ├── Dockerfile                     # Multi-stage Dockerfile cho Frontend (Nginx)
│   ├── nginx.conf                     # Cấu hình Nginx phục vụ SPA & Reverse Proxy
│   ├── package.json
│   └── vite.config.js
├── src/
│   ├── main/
│   │   ├── java/com/bluesky/pos_system/
│   │   │   ├── configuration/         # Security, Redis, Stripe, OpenAPI configs
│   │   │   ├── controllers/           # REST API Controllers
│   │   │   ├── models/                # JPA Database Entities
│   │   │   ├── repositories/          # Spring Data JPA Repositories
│   │   │   ├── services/              # Business Logic Interfaces & Impls
│   │   │   ├── mappers/               # Entity <-> DTO Mappers
│   │   │   ├── payload/               # Requests, Responses, DTOs
│   │   │   └── exceptions/            # Global Exception Handler
│   │   └── resources/
│   │       ├── db/migration/          # Flyway SQL scripts (V1 -> V4)
│   │       ├── application.properties # Cấu hình chung
│   │       └── application-prod.properties # Cấu hình tối ưu Production
│   └── test/                          # Unit Tests & Integration Tests
├── .env.example                       # File mẫu biến môi trường
├── checkstyle.xml                     # Quy chuẩn kiểm tra Code Style
├── Dockerfile                         # Multi-stage Dockerfile cho Backend
├── docker-compose.yml                 # Khởi chạy toàn bộ hệ thống (MySQL, Redis, App, UI)
├── pom.xml                            # Quản lý dependencies & plugins Maven
└── README.md
```

---

## 4. Yêu Cầu Môi Trường (Prerequisites)

- **JDK**: Java 17 hoặc Java 21 LTS
- **Node.js**: Phiên bản 20.x trở lên (kèm `npm`)
- **Maven**: 3.9+ (hoặc dùng trực tiếp `./mvnw` / `mvnw.cmd` có sẵn)
- **Cơ sở dữ liệu**: MySQL 8.0+ và Redis 7+ (hoặc chạy qua Docker)

---

## 5. Hướng Dẫn Chạy Môi Trường Local

### Bước 1: Khởi động MySQL & Redis
Nếu máy đã cài sẵn Docker:
```bash
docker run -d --name skypos-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=pos_system mysql:8.0
docker run -d --name skypos-redis -p 6379:6379 redis:7-alpine
```

### Bước 2: Khởi chạy Backend API (Port 5000)
1. Tạo file `.env` từ file mẫu:
   ```bash
   cp .env.example .env
   ```
2. Chạy ứng dụng qua Maven Wrapper:
   ```bash
   # Windows PowerShell
   .\mvnw.cmd spring-boot:run

   # Linux / macOS
   ./mvnw spring-boot:run
   ```
3. Sau khi khởi động thành công, hệ thống tự động sinh dữ liệu mẫu:
   - **Tài khoản Admin mặc định**: `admin@pos.com` / `admin123`
   - **Swagger UI**: [http://localhost:5000/swagger-ui/index.html](http://localhost:5000/swagger-ui/index.html)
   - **Health Check**: [http://localhost:5000/actuator/health](http://localhost:5000/actuator/health)

### Bước 3: Khởi chạy Frontend UI (Port 5173)
```bash
cd pos-ui
npm install
npm run dev
```
Truy cập giao diện tại: [http://localhost:5173](http://localhost:5173)

---

## 6. Hướng Dẫn Chạy Bằng Docker & Docker Compose

Chỉ cần một lệnh duy nhất để khởi động toàn bộ stack gồm **MySQL + Redis + Backend Spring Boot + Frontend Nginx**:

```bash
# Khởi chạy toàn bộ hệ thống ở chế độ background
docker compose up -d --build
```

Kiểm tra trạng thái container:
```bash
docker compose ps
```

| Dịch Vụ | Container Name | Port Host | Mô Tả |
|---|---|---|---|
| **Frontend** | `skypos-frontend` | `http://localhost:80` | Giao diện Web bán hàng React Nginx |
| **Backend** | `skypos-backend` | `http://localhost:5000` | REST API Spring Boot |
| **MySQL** | `skypos-mysql` | `localhost:3307` | Database lưu trữ dữ liệu chính |
| **Redis** | `skypos-redis` | `localhost:6379` | Cache dữ liệu & phiên làm việc |

Dừng hệ thống:
```bash
docker compose down
```

---

## 7. Biến Môi Trường (Environment Variables)

| Tên Biến | Mặc Định / Ví Dụ | Bắt Buộc | Mô Tả |
|---|---|:---:|---|
| `SERVER_PORT` | `5000` | Không | Port lắng nghe của backend |
| `SPRING_PROFILE` | `dev` / `prod` | Có | Profile kích hoạt cấu hình (`dev`, `prod`) |
| `DB_URL` | `jdbc:mysql://localhost:3307/pos_system...` | Có | JDBC Connection URL đến MySQL |
| `DB_USERNAME` | `root` | Có | Tên người dùng database |
| `DB_PASSWORD` | `your_db_password` | Có | Mật khẩu database |
| `REDIS_HOST` | `localhost` / `redis` | Có | Hostname của dịch vụ Redis |
| `REDIS_PORT` | `6379` | Không | Port kết nối Redis |
| `JWT_SECRET` | Chuỗi bí mật >= 64 ký tự | Có | Secret key giải mã HMAC-SHA512 |
| `JWT_EXPIRATION_MS` | `86400000` (24 giờ) | Không | Thời gian sống của JWT token |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost` | Có | Whitelist domain được phép gọi API |
| `STRIPE_SECRET_KEY` | `sk_test_...` | Tùy chọn | API Secret Key của Stripe Payment |
| `STRIPE_PUBLISHABLE_KEY` | `pk_test_...` | Tùy chọn | Publishable Key cho Stripe Elements UI |
| `STRIPE_WEBHOOK_SECRET` | `whsec_...` | Tùy chọn | Chữ ký xác thực webhook từ Stripe |
| `STRIPE_DEFAULT_CURRENCY` | `vnd` / `usd` | Không | Đơn vị tiền tệ mặc định |

---

## 8. API Reference & Danh Sách Endpoints Cốt Lõi

API Base URL: `http://localhost:5000/api` (hoặc `/api/v1`)  
Đăng nhập nhận Token: `POST /auth/login`

| Nhóm Chức Năng | Method | Đường Dẫn (Endpoint) | Quyền Hạn | Mô Tả |
|---|:---:|---|:---:|---|
| **Xác thực** | `POST` | `/auth/login` | Public | Đăng nhập nhận JWT Token & Profile |
| | `POST` | `/auth/signup` | Public | Đăng ký tài khoản mới |
| **Cửa hàng** | `GET` | `/api/stores` | Authenticated | Danh sách tất cả cửa hàng |
| | `GET` | `/api/stores/{id}` | Authenticated | Chi tiết cửa hàng theo ID |
| **Chi nhánh** | `GET` | `/api/branches/store/{storeId}` | Authenticated | Danh sách chi nhánh của cửa hàng |
| | `POST` | `/api/branches` | `ADMIN` | Tạo mới chi nhánh |
| **Danh mục** | `GET` | `/api/categories/store/{storeId}` | Authenticated | Danh sách cây phân cấp danh mục |
| **Sản phẩm** | `GET` | `/api/products/store/{storeId}` | Authenticated | Danh sách sản phẩm (có lọc, tìm kiếm) |
| | `POST` | `/api/products` | `ADMIN`, `MANAGER` | Tạo mới sản phẩm |
| **Kho hàng** | `GET` | `/api/inventories/branch/{branchId}` | Authenticated | Tồn kho thực tế tại chi nhánh |
| | `POST` | `/api/inventories/adjust` | `MANAGER`, `STAFF` | Điều chỉnh tăng/giảm tồn kho & ghi sổ cái |
| | `GET` | `/api/inventories/branch/{branchId}/summary` | Authenticated | Thống kê số lượng tồn, cảnh báo sắp hết |
| **Đơn hàng** | `POST` | `/api/orders` | `CASHIER`, `ADMIN` | Tạo đơn hàng POS mới |
| | `GET` | `/api/orders/branch/{branchId}` | Authenticated | Danh sách đơn hàng theo chi nhánh |
| | `GET` | `/api/orders/{id}` | Authenticated | Chi tiết hóa đơn (kèm danh sách món) |
| **Thanh toán** | `POST` | `/api/payments/stripe/create-intent` | Authenticated | Khởi tạo PaymentIntent với Stripe |
| | `POST` | `/api/payments/stripe/refund` | `ADMIN`, `MANAGER` | Hoàn tiền giao dịch Stripe |
| | `GET` | `/api/payments/transactions/order/{orderId}` | Authenticated | Lịch sử giao dịch thanh toán của đơn |
| **Ca thu ngân** | `GET` | `/api/shift-reports/current` | Authenticated | Ca làm việc hiện tại của thu ngân |
| | `POST` | `/api/shift-reports/start` | `CASHIER` | Mở ca làm việc mới |
| | `POST` | `/api/shift-reports/close` | `CASHIER` | Kết thúc ca làm việc và chốt tiền |

---

## 9. Hướng Dẫn Kiểm Thử & Đảm Bảo Chất Lượng Code

### Chạy Unit & Integration Tests:
```bash
# Chạy toàn bộ test suites
./mvnw clean test

# Chạy riêng một nhóm test cụ thể
./mvnw test -Dtest="OrderServiceTest,PaymentServiceTest,InventoryServiceTest"

# Xuất báo cáo độ phủ mã nguồn JaCoCo
./mvnw jacoco:report
```
*Báo cáo độ phủ HTML được tạo tại:* `target/site/jacoco/index.html`

### Kiểm tra Tiêu Chuẩn Code (Static Analysis):
```bash
# Kiểm tra định dạng và quy chuẩn Java qua Checkstyle
./mvnw checkstyle:check

# Kiểm tra cú pháp Frontend qua ESLint
cd pos-ui
npm run lint
```

---

## 10. CI/CD & Hướng Dẫn Triển Khai Production

Dự án đã tích hợp sẵn **GitHub Actions Workflow** tại file `.github/workflows/ci.yml`.

### Quy trình tự động hóa:
1. **Pull Request & Push**:
   - Tự động dựng môi trường MySQL Test Container.
   - Chạy kiểm tra Code Quality qua Checkstyle.
   - Thực thi toàn bộ Unit Test và Integration Test.
   - Đo lường và lưu trữ báo cáo Coverage JaCoCo.
   - Cài đặt dependencies và kiểm tra build Frontend React.
2. **Release Tag (`v*.*.*`)**:
   - Tự động đóng gói Docker Images cho cả Backend và Frontend.
   - Đẩy Images lên GitHub Container Registry (GHCR).

### Lưu ý sống còn khi triển khai Production:
- [ ] Đổi `pos.jwt.secret` sang chuỗi bảo mật ngẫu nhiên ít nhất 64 ký tự.
- [ ] Đặt `spring.jpa.hibernate.ddl-auto=none` để Flyway hoàn toàn kiểm soát schema.
- [ ] Cấu hình Whitelist `pos.cors.allowed-origins` với domain thật, tuyệt đối không dùng `*`.
- [ ] Bảo vệ endpoint Prometheus và Actuator (`management.endpoint.health.show-details=never`).
- [ ] Đảm bảo Webhook URL của Stripe được trỏ đúng về `POST /api/payments/stripe/webhook` kèm `STRIPE_WEBHOOK_SECRET` hợp lệ.

---
**SkyPOS Team** - *Sẵn sàng triển khai, mở rộng và vận hành bền bỉ.*
